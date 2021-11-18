package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;
import uk.ac.ceh.gateway.catalogue.quality.MetadataCheck;
import uk.ac.ceh.gateway.catalogue.quality.Results;

import java.util.*;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.ERROR;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.INFO;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class ServiceAgreementQualityService {

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

            if (isQualifyingDocument(parsedMeta)) {
                val checks = new ArrayList<MetadataCheck>();
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

    Optional<List<MetadataCheck>> checkBasics(DocumentContext parsedDoc) {
        val requiredKeys = ImmutableSet.of("title", "description", "depositorContactDetails");
        val toReturn = new ArrayList<MetadataCheck>();
        val toCheck = parsedDoc.read(
                "$.['title','description', 'depositorContactDetails']",
                new TypeRef<Map<String, String>>() {
                }
        );
        requiredKeys.forEach(key -> {
            if (fieldIsMissing(toCheck, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });

        try {
            val description = parsedDoc.read("$.description", String.class).trim();
            if (description.trim().length() < 100) {
                toReturn.add(new MetadataCheck("Description is too short (minimum 100 characters)", ERROR));
            }
        } catch (NullPointerException ex) {
            toReturn.add(new MetadataCheck("Description is too short (minimum 100 characters)", ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkAuthors(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val authors = parsed.read(
                "$.authors[*][?(@.role == 'author')].['individualName', 'organisationName','email']",
                typeRefStringString
        );
        if (authors.size() == 0) {
            toReturn.add(new MetadataCheck("There are no authors", INFO));
        }
        checkAddress(authors, "Author").ifPresent(toReturn::addAll);

        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "individualName"))) {
            toReturn.add(new MetadataCheck("Author's name is missing", INFO));
        }
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "organisationName"))) {
            toReturn.add(new MetadataCheck("Author's affiliation (organisation name) is missing", ERROR));
        }
        authors.stream()
                .filter(author -> author.containsKey("email"))
                .map(author -> author.get("email"))
                .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk") && !email.equals("info@eidc.ac.uk"))
                .forEach(email -> toReturn.add(new MetadataCheck(format("Author's email address is %s", email), ERROR)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkOwnerOfIpr(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val owners = parsed.read(
                "$.ownersOfIpr[*].['individualName', 'organisationName','email']",
                typeRefStringString
        );

        if (owners.size() == 0) {
            toReturn.add(new MetadataCheck("There are no owners", INFO));
        }

        checkAddress(owners, "Owner").ifPresent(toReturn::addAll);

        if (owners.stream().anyMatch(owner -> fieldIsMissing(owner, "email"))) {
            toReturn.add(new MetadataCheck("Owner's email address is missing", ERROR));
        }

        if (owners.stream().anyMatch(owner -> fieldIsMissing(owner, "organisationName"))) {
            toReturn.add(new MetadataCheck("Owner's affiliation (organisation name) is missing", ERROR));
        }
        owners.stream()
                .filter(owner -> owner.containsKey("email"))
                .map(owner -> owner.get("email"))
                .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk") && !email.equals("info@eidc.ac.uk"))
                .forEach(email -> toReturn.add(new MetadataCheck(format("Author's email address is %s", email), ERROR)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkAddress(List<Map<String, String>> addresses, String element) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (addresses == null || addresses.isEmpty()) {
            return Optional.of(Collections.singletonList(new MetadataCheck(element + " is missing", ERROR)));
        }
        if (addresses.stream().anyMatch(map -> fieldIsMissing(map, "organisationName"))) {
            toReturn.add(new MetadataCheck(element + " organisation name is missing", ERROR));
        }
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkDepositorContactDetails(DocumentContext parsed) {

        val toReturn = new ArrayList<MetadataCheck>();
        val depositorContactDetails = parsed.read("$.depositorContactDetails", String.class).trim();

        if (depositorContactDetails.isEmpty()) {
            toReturn.add(new MetadataCheck("Depositor's email address is missing", ERROR));
        }

        if(depositorContactDetails.equals("enquiries@ceh.ac.uk") || depositorContactDetails.equals("info@eidc.ac.uk")){
            toReturn.add(new MetadataCheck(format("Depositor's email address is %s", depositorContactDetails), ERROR));
        }

        if(!depositorContactDetails.endsWith("@ceh.ac.uk")){
            toReturn.add(new MetadataCheck(format("Depositor's email address is %s which is not a CEH email address", depositorContactDetails), ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkFiles(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val files = parsed.read(
                "$.files[*].['name','format', 'size']",
                typeRefStringString
        );

        if (files.stream().anyMatch(file -> fieldIsMissing(file, "name"))) {
            toReturn.add(new MetadataCheck("File name is missing", ERROR));
        }else{
            if (files.stream().anyMatch(file -> file.get("name").contains(" "))) {
                toReturn.add(new MetadataCheck("File names should not contain any spaces", ERROR));
            }
            if (!files.stream().anyMatch(file -> file.get("name").matches("^[\\w\\-\\_]*\\.\\w+?$"))) {
                toReturn.add(new MetadataCheck("File names should only consist of alphanumeric characters, underscore, hyphen and dots", ERROR));
            }
        }
        if (files.stream().anyMatch(file -> fieldIsMissing(file, "format"))) {
            toReturn.add(new MetadataCheck("File format is missing", ERROR));
        }
        if (files.stream().anyMatch(file -> fieldIsMissing(file, "size"))) {
            toReturn.add(new MetadataCheck("File size is missing", ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    private boolean isQualifyingDocument(DocumentContext parsedMeta) {
        val docType = parsedMeta.read("$.documentType", String.class);
        return docType != null;
    }

    private boolean fieldIsMissing(Map<String, String> map, String key) {
        return map == null
                || !map.containsKey(key)
                || map.get(key) == null
                || map.get(key).isEmpty();
    }
}