package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.DocumentTypes.SERVICE_AGREEMENT;
import static uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Severity.*;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class ServiceAgreementQualityService {
    // Valid emails address, author names,
    // file names as these need to be correct before being inserted into the metadata record

    private final DocumentReader documentReader;
    private final Configuration config;

    private final Set<String> validResourceTypes = ImmutableSet.of(
            "service-agreement"
    );

    private final TypeRef<List<Map<String, String>>> typeRefStringString = new TypeRef<>() {
    };

    private static final String FOLDER = "service-agreements/";

    public ServiceAgreementQualityService(
            @NonNull DocumentReader documentReader,
            @NonNull ObjectMapper objectMapper
    ) {
        this.documentReader = documentReader;
        this.config = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider(objectMapper))
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(
                        Option.DEFAULT_PATH_LEAF_TO_NULL,
                        Option.SUPPRESS_EXCEPTIONS
                );
        log.info("Creating");
    }

    @SneakyThrows
    public MetadataQualityService.Results check(String id) {
        log.debug("Checking {}", id);

        try {
            val parsedDoc = JsonPath.parse(
                    documentReader.read(FOLDER + id, "raw"),
                    config
            );
            val parsedMeta = JsonPath.parse(
                    documentReader.read(FOLDER + id, "meta"),
                    config
            );

            if (isQualifyingDocument(parsedDoc, parsedMeta)) {
                val checks = new ArrayList<MetadataQualityService.MetadataCheck>();
                checkBasics(parsedDoc).ifPresent(checks::addAll);
                checkAuthors(parsedDoc).ifPresent(checks::addAll);
                checkPublisher(parsedDoc).ifPresent(checks::addAll);
                return new MetadataQualityService.Results(checks, id);
            } else {
                return new MetadataQualityService.Results(Collections.emptyList(), id, "Not a qualifying document type");
            }
        } catch (Exception ex) {
            log.error("Error - could not check " + id, ex);
            return new MetadataQualityService.Results(Collections.emptyList(), id, "Error - could not check this document");
        }
    }

    Optional<List<MetadataQualityService.MetadataCheck>> checkBasics(DocumentContext parsedDoc) {
        val requiredKeys = ImmutableSet.of("title", "description", "resourceType", "depositorContactDetails");
        val toReturn = new ArrayList<MetadataQualityService.MetadataCheck>();
        val toCheck = parsedDoc.read(
                "$.['title','description']",
                new TypeRef<Map<String, String>>() {
                }
        );
        toCheck.put("resourceType", parsedDoc.read("$.resourceType.value", String.class));
        requiredKeys.forEach(key -> {
            if (fieldIsMissing(toCheck, key)) {
                toReturn.add(new MetadataQualityService.MetadataCheck(key + " is missing", ERROR));
            }
        });

        try {
            val description = parsedDoc.read("$.description", String.class).trim();
            if (description.trim().length() < 100) {
                toReturn.add(new MetadataQualityService.MetadataCheck("Description is too short (minimum 100 characters)", ERROR));
            }
        } catch (NullPointerException ex) {
            toReturn.add(new MetadataQualityService.MetadataCheck("Description is too short (minimum 100 characters)", ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataQualityService.MetadataCheck>> checkAuthors(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataQualityService.MetadataCheck>();
        val authors = parsed.read(
                "$.responsibleParties[*][?(@.role == 'author')].['individualName', 'organisationName','email']",
                typeRefStringString
        );
        if (authors.size() == 0) {
            toReturn.add(new MetadataQualityService.MetadataCheck("There are no authors", INFO));
        }
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "individualName"))) {
            toReturn.add(new MetadataQualityService.MetadataCheck("Author's name is missing", INFO));
        }
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "organisationName"))) {
            toReturn.add(new MetadataQualityService.MetadataCheck("Author's affiliation (organisation name) is missing", ERROR));
        }
        authors.stream()
                .filter(author -> author.containsKey("email"))
                .map(author -> author.get("email"))
                .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk") && !email.equals("info@eidc.ac.uk"))
                .forEach(email -> toReturn.add(new MetadataQualityService.MetadataCheck(format("Author's email address is %s", email), ERROR)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataQualityService.MetadataCheck>> checkPublisher(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataQualityService.MetadataCheck>();
        val publishers = parsed.read(
                "$.responsibleParties[*][?(@.role == 'publisher')].['organisationName','email']",
                typeRefStringString
        );
        checkAddress(publishers, "Publisher").ifPresent(toReturn::addAll);
        if (publishers.size() > 1) {
            toReturn.add(new MetadataQualityService.MetadataCheck("There should be only ONE publisher", ERROR));
        }
        if (publishers.stream().anyMatch(publisher -> fieldIsMissing(publisher, "email"))) {
            toReturn.add(new MetadataQualityService.MetadataCheck("Publisher email address is missing", ERROR));
        }
        publishers.stream()
                .filter(publisher -> fieldNotEqual(publisher, "organisationName", "NERC EDS Environmental Information Data Centre") && fieldNotEqual(publisher, "organisationName", "NERC Environmental Information Data Centre"))
                .map(publisher -> publisher.getOrDefault("organisationName", "unknown"))
                .forEach(organisationName -> toReturn.add(new MetadataQualityService.MetadataCheck("Publisher name is " + organisationName, WARNING)));

        publishers.stream()
                .filter(publisher -> fieldNotEqual(publisher, "email", "info@eidc.ac.uk"))
                .map(publisher -> publisher.getOrDefault("email", "unknown"))
                .forEach(email -> toReturn.add(new MetadataQualityService.MetadataCheck("Publishers email address is " + email, INFO)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataQualityService.MetadataCheck>> checkAddress(List<Map<String, String>> addresses, String element) {
        val toReturn = new ArrayList<MetadataQualityService.MetadataCheck>();
        if (addresses == null || addresses.isEmpty()) {
            return Optional.of(Collections.singletonList(new MetadataQualityService.MetadataCheck(element + " is missing", ERROR)));
        }
        if (addresses.stream().anyMatch(map -> fieldIsMissing(map, "organisationName"))) {
            toReturn.add(new MetadataQualityService.MetadataCheck(element + " organisation name is missing", ERROR));
        }
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    private boolean isQualifyingDocument(DocumentContext parsedDoc, DocumentContext parsedMeta) {
        val docType = parsedMeta.read("$.documentType", String.class);
        log.info("DOCUMENT TYPE = {} HERE!!!", docType);
        return docType != null && docType.equals(SERVICE_AGREEMENT);
    }

    private boolean fieldIsMissing(Map<String, String> map, String key) {
        return map == null
                || !map.containsKey(key)
                || map.get(key) == null
                || map.get(key).isEmpty();
    }

    private boolean fieldNotEqual(Map<String, String> map, String key, String value) {
        return !map.containsKey(key)
                || map.get(key) == null
                || !map.get(key).equals(value);
    }

    public enum Severity {
        ERROR(1), WARNING(2), INFO(3);

        private final int priority;

        Severity(int priority) {
            this.priority = priority;
        }
    }

    @Value
    public static class MetadataCheck {
        String test;
        Severity severity;
    }


    @Value
    public static class Results {
        List<MetadataCheck> problems;
        String id, message;

        public Results(@NonNull List<MetadataCheck> problems, @NonNull String id) {
            this(problems, id, "");
        }

        public Results(@NonNull List<MetadataCheck> problems, @NonNull String id, @NonNull String message) {
            this.problems = problems.stream()
                    .sorted(Comparator.comparingInt(o -> o.getSeverity().priority))
                    .collect(Collectors.toList());
            this.id = id;
            this.message = message;
        }

        public long getErrors() {
            return problems.stream()
                    .filter(m -> ERROR.equals(m.getSeverity()))
                    .count();
        }

        public long getWarnings() {
            return problems.stream()
                    .filter(m -> WARNING.equals(m.getSeverity()))
                    .count();
        }

        public long getInfo() {
            return problems.stream()
                    .filter(m -> INFO.equals(m.getSeverity()))
                    .count();
        }
    }

    @Value
    public static class ServiceAgreementResults {
        List<MetadataQualityService.Results> results;

        public long getTotalErrors() {
            return results.stream()
                    .mapToLong(MetadataQualityService.Results::getErrors)
                    .sum();
        }

        public long getTotalWarnings() {
            return results.stream()
                    .mapToLong(MetadataQualityService.Results::getWarnings)
                    .sum();
        }
    }
}