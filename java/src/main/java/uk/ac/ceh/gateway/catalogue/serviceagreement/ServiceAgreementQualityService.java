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

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.DocumentTypes.SERVICE_AGREEMENT;
import static uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementQualityService.Severity.*;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class ServiceAgreementQualityService {
    // Valid address, author names,
    // file names as these need to be correct before being inserted into the metadata record

    private final DocumentReader documentReader;
    private final Configuration config;

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
    public Results check(String id) {
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
                val checks = new ArrayList<ServiceAgreementCheck>();
                checkBasics(parsedDoc).ifPresent(checks::addAll);
                checkAuthors(parsedDoc).ifPresent(checks::addAll);
                checkDepositorContactDetails(parsedDoc).ifPresent(checks::addAll);
                checkOwnerOfIpr(parsedDoc).ifPresent(checks::addAll);
                checkFiles(parsedDoc).ifPresent(checks::addAll);
                return new Results(checks, id);
            } else {
                return new Results(Collections.emptyList(), id, "Not a qualifying document type");
            }
        } catch (Exception ex) {
            log.error("Error - could not check " + id, ex);
            return new Results(Collections.emptyList(), id, "Error - could not check this document");
        }
    }

    Optional<List<ServiceAgreementCheck>> checkBasics(DocumentContext parsedDoc) {
        val requiredKeys = ImmutableSet.of("title", "description");
        val toReturn = new ArrayList<ServiceAgreementCheck>();
        val toCheck = parsedDoc.read(
                "$.['title','description']",
                new TypeRef<Map<String, String>>() {
                }
        );
        requiredKeys.forEach(key -> {
            if (fieldIsMissing(toCheck, key)) {
                toReturn.add(new ServiceAgreementCheck(key + " is missing", ERROR));
            }
        });

        try {
            val description = parsedDoc.read("$.description", String.class).trim();
            if (description.trim().length() < 100) {
                toReturn.add(new ServiceAgreementCheck("Description is too short (minimum 100 characters)", ERROR));
            }
        } catch (NullPointerException ex) {
            toReturn.add(new ServiceAgreementCheck("Description is too short (minimum 100 characters)", ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<ServiceAgreementCheck>> checkAuthors(DocumentContext parsed) {
        val toReturn = new ArrayList<ServiceAgreementCheck>();
        val authors = parsed.read(
                "$.authors[*][?(@.role == 'author')].['individualName', 'organisationName','email']",
                typeRefStringString
        );
        if (authors.size() == 0) {
            toReturn.add(new ServiceAgreementCheck("There are no authors", INFO));
        }
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "individualName"))) {
            toReturn.add(new ServiceAgreementCheck("Author's name is missing", INFO));
        }
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "organisationName"))) {
            toReturn.add(new ServiceAgreementCheck("Author's affiliation (organisation name) is missing", ERROR));
        }
        authors.stream()
                .filter(author -> author.containsKey("email"))
                .map(author -> author.get("email"))
                .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk") && !email.equals("info@eidc.ac.uk"))
                .forEach(email -> toReturn.add(new ServiceAgreementCheck(format("Author's email address is %s", email), ERROR)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<ServiceAgreementCheck>> checkOwnerOfIpr(DocumentContext parsed) {
        val toReturn = new ArrayList<ServiceAgreementCheck>();
        val owners = parsed.read(
                "$.ownersOfIpr[*].['organisationName','email']",
                typeRefStringString
        );
        checkAddress(owners, "Owner").ifPresent(toReturn::addAll);

        if (owners.stream().anyMatch(owner -> fieldIsMissing(owner, "email"))) {
            toReturn.add(new ServiceAgreementCheck("Owner's email address is missing", ERROR));
        }
        owners.stream()
                .filter(owner -> fieldNotEqual(owner, "organisationName", "NERC EDS Environmental Information Data Centre") && fieldNotEqual(owner, "organisationName", "NERC Environmental Information Data Centre"))
                .map(owner -> owner.getOrDefault("organisationName", "unknown"))
                .forEach(organisationName -> toReturn.add(new ServiceAgreementCheck("Owner name is " + organisationName, WARNING)));

        owners.stream()
                .filter(owner -> fieldNotEqual(owner, "email", "info@eidc.ac.uk"))
                .map(owner -> owner.getOrDefault("email", "unknown"))
                .forEach(email -> toReturn.add(new ServiceAgreementCheck("Owner email address is " + email, INFO)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<ServiceAgreementCheck>> checkAddress(List<Map<String, String>> addresses, String element) {
        val toReturn = new ArrayList<ServiceAgreementCheck>();
        if (addresses == null || addresses.isEmpty()) {
            return Optional.of(Collections.singletonList(new ServiceAgreementCheck(element + " is missing", ERROR)));
        }
        if (addresses.stream().anyMatch(map -> fieldIsMissing(map, "organisationName"))) {
            toReturn.add(new ServiceAgreementCheck(element + " organisation name is missing", ERROR));
        }
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<ServiceAgreementCheck>> checkDepositorContactDetails(DocumentContext parsed) {

        val toReturn = new ArrayList<ServiceAgreementCheck>();
        val depositorContactDetails = parsed.read("$.depositorContactDetails", String.class).trim();
        if(depositorContactDetails.endsWith("@ceh.ac.uk") && !depositorContactDetails.equals("enquiries@ceh.ac.uk") && !depositorContactDetails.equals("info@eidc.ac.uk")){
            toReturn.add(new ServiceAgreementCheck(format("Author's email address is %s", depositorContactDetails), ERROR));
        }
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<ServiceAgreementCheck>> checkFiles(DocumentContext parsed) {
        val toReturn = new ArrayList<ServiceAgreementCheck>();
        val files = parsed.read(
                "$.files[*].['name','format', 'size']",
                typeRefStringString
        );

        if (files.stream().anyMatch(file -> fieldIsMissing(file, "name"))) {
            toReturn.add(new ServiceAgreementCheck("File name is missing", ERROR));
        }else{
            if (files.stream().anyMatch(file -> file.get("name").contains(" "))) {
                toReturn.add(new ServiceAgreementCheck("File names should not contain any spaces", ERROR));
            }
            if (!files.stream().anyMatch(file -> file.get("name").matches("^[\\w\\-\\_]?\\.\\w$"))) {
                toReturn.add(new ServiceAgreementCheck("File names should only consist of alphanumeric characters, underscore, hyphen and dots", ERROR));
            }
        }
        if (files.stream().anyMatch(file -> fieldIsMissing(file, "format"))) {
            toReturn.add(new ServiceAgreementCheck("File format is missing", ERROR));
        }
        if (files.stream().anyMatch(file -> fieldIsMissing(file, "size"))) {
            toReturn.add(new ServiceAgreementCheck("File size is missing", ERROR));
        }

        files.stream()
                .filter(file -> fieldNotEqual(file, "organisationName", "NERC EDS Environmental Information Data Centre") && fieldNotEqual(file, "organisationName", "NERC Environmental Information Data Centre"))
                .map(owner -> owner.getOrDefault("organisationName", "unknown"))
                .forEach(organisationName -> toReturn.add(new ServiceAgreementCheck("Owner name is " + organisationName, WARNING)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    private boolean isQualifyingDocument(DocumentContext parsedDoc, DocumentContext parsedMeta) {
        val docType = parsedMeta.read("$.documentType", String.class);
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

    @Value
    public static class ServiceAgreementCheck {
        String test;
        Severity severity;
    }

    public enum Severity {
        ERROR(1), WARNING(2), INFO(3);

        private final int priority;

        Severity(int priority) {
            this.priority = priority;
        }
    }

    @Value
    public static class Results {
        List<ServiceAgreementCheck> problems;
        String id, message;

        public Results(@NonNull List<ServiceAgreementCheck> problems, @NonNull String id) {
            this(problems, id, "");
        }

        public Results(@NonNull List<ServiceAgreementCheck> problems, @NonNull String id, @NonNull String message) {
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
        List<Results> results;

        public long getTotalErrors() {
            return results.stream()
                    .mapToLong(Results::getErrors)
                    .sum();
        }

        public long getTotalWarnings() {
            return results.stream()
                    .mapToLong(Results::getWarnings)
                    .sum();
        }
    }
}