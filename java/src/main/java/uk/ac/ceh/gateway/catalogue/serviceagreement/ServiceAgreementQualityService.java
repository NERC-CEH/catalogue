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
import org.springframework.beans.factory.annotation.Value;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;
import uk.ac.ceh.gateway.catalogue.quality.MetadataCheck;
import uk.ac.ceh.gateway.catalogue.quality.Results;

import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.ERROR;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.INFO;
import static uk.ac.ceh.gateway.catalogue.serviceagreement.GitRepoServiceAgreementService.FOLDER;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class ServiceAgreementQualityService {

    private final DocumentReader documentReader;
    private final Configuration config;
    private final Pattern AUTHOR_PATTERN = Pattern.compile("^[\\w\\-\\s']+, (\\w\\.){1,5}$");
    private final Pattern EMAIL_PATTERN = Pattern.compile("^[a-z0-9\\\\!#$%&'*+/=?^_`{|}~\\-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~\\-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
    private final String jiraPrefix;

    private final TypeRef<List<Map<String, String>>> typeRefStringString = new TypeRef<>() {
    };

    public ServiceAgreementQualityService(
            @NonNull DocumentReader documentReader,
            @NonNull ObjectMapper objectMapper,
            @NonNull @Value("${jira.serviceAgreement.prefix}") String jiraPrefix
            ) {
        this.documentReader = documentReader;
        this.config = Configuration.defaultConfiguration()
                .jsonProvider(new JacksonJsonProvider(objectMapper))
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .addOptions(
                        Option.DEFAULT_PATH_LEAF_TO_NULL,
                        Option.SUPPRESS_EXCEPTIONS
                );
        this.jiraPrefix = jiraPrefix;
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
                checkDiscoveryMetadata(parsedDoc).ifPresent(checks::addAll);
                checkAuthors(parsedDoc).ifPresent(checks::addAll);
                checkSupportingDocs(parsedDoc).ifPresent(checks::addAll);
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
        val requiredKeys = ImmutableSet.of("title", "depositorContactDetails", "eidcName", "transferMethod", "depositReference");
        val toReturn = new ArrayList<MetadataCheck>();
        val toCheck = parsedDoc.read(
                "$.['title', 'depositorContactDetails', 'eidcName', 'transferMethod', 'depositReference']",
                new TypeRef<Map<String, String>>() {
                }
        );

        requiredKeys.forEach(key -> {
            if (fieldIsMissing(toCheck, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });

        try {
            val depositReference = parsedDoc.read("$.depositReference", String.class).trim();
            if (!depositReference.matches("^"+jiraPrefix+"\\d{1,9}$")) {
                toReturn.add(new MetadataCheck("Deposit reference must be present and must match "+jiraPrefix+"XXXX", ERROR));
            }
        } catch (NullPointerException ex) {
            toReturn.add(new MetadataCheck("Deposit reference must be present and must match "+jiraPrefix+"XXXX", ERROR));
        }


        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkDiscoveryMetadata(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val boundingBoxes = parsed.read(
            "$.boundingBoxes[*]",
            typeRefStringString
        );

        if (boundingBoxes.size() == 0 ) {
            toReturn.add(new MetadataCheck("Area of study not included", INFO));
        }

        try {
            val lineage = parsed.read("$.lineage", String.class).trim();
            if (stringIsMissing(lineage)) {
                toReturn.add(new MetadataCheck("Lineage is incomplete", ERROR));
            }
        } catch (NullPointerException ex) {
            toReturn.add(new MetadataCheck("Lineage is incomplete", ERROR));
        }

        try {
            val description = parsed.read("$.description", String.class).trim();
            if (!stringIsMissing(description) && description.length() < 100)  {
                toReturn.add(new MetadataCheck("Description is incomplete (minimum 100 characters)", ERROR));
            }
        } catch (NullPointerException ex) {
            toReturn.add(new MetadataCheck("Description is incomplete (minimum 100 characters)", ERROR));
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
            toReturn.add(new MetadataCheck("There are no authors", ERROR));
        }

        if (authors.size() >= 1 ) {
            if (authors.stream().anyMatch(author -> fieldIsMissing(author, "individualName"))) {
                toReturn.add(new MetadataCheck("Author's name is missing", ERROR));
            } else {
                if (authors.stream().noneMatch(author -> AUTHOR_PATTERN.matcher(author.get("individualName")).matches())) {
                    toReturn.add(new MetadataCheck("Author name format incorrect", ERROR));
                }
            }

            if (authors.stream().anyMatch(author -> fieldIsMissing(author, "organisationName"))) {
                toReturn.add(new MetadataCheck("Author's affiliation (organisation name) is missing", ERROR));
            }

            checkEmail(authors, "Author's email address is incorrect (%s)").ifPresent(toReturn::addAll);
        }

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
            toReturn.add(new MetadataCheck("There are no IPR owners", ERROR));
        }

        if (owners.stream().anyMatch(owner -> fieldIsMissing(owner, "organisationName"))) {
            toReturn.add(new MetadataCheck("IPR owner's affiliation (organisation name) is missing", ERROR));
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

        if (isInvalidEmail(depositorContactDetails)) {
            toReturn.add(new MetadataCheck(format("Depositor's email address is incorrect  (%s)", depositorContactDetails), ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkFiles(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val fileNamingConvention = parsed.read("$.fileNamingConvention", String.class);
        val fileNumber = parsed.read("$.fileNumber", String.class);
        val files = parsed.read(
                "$.files[*].['name','format', 'size']",
                typeRefStringString
        );

        if (files.size() == 0 && stringIsMissing(fileNamingConvention)) {
            toReturn.add(new MetadataCheck("You must describe the files to be deposited (either a list of files or a naming convention)", ERROR));
        }

        if (files.size() >=1 && !stringIsMissing(fileNamingConvention)) {
            toReturn.add(new MetadataCheck("You should EITHER list the files to be deposited OR  a naming convention, not both)", INFO));
        }

        if (stringIsMissing(fileNumber)) {
            toReturn.add(new MetadataCheck("Number of files to be deposited is missing", ERROR));
        }

        if (files.size() >= 1 ) {
            if (files.stream().anyMatch(file -> fieldIsMissing(file, "name"))) {
                toReturn.add(new MetadataCheck("File name is missing", ERROR));
            } else {
                if (files.stream().anyMatch(file -> file.get("name").contains(" "))) {
                    toReturn.add(new MetadataCheck("File names should not contain any spaces", ERROR));
                }
                if (files.stream().noneMatch(file -> file.get("name").matches("^[\\w\\-\\_\\.]*$"))) {
                    toReturn.add(new MetadataCheck("File names should only consist of alphanumeric characters, underscore and hyphens", ERROR));
                }
            }

            if (files.stream().anyMatch(file -> fieldIsMissing(file, "format"))) {
                toReturn.add(new MetadataCheck("File format is missing", ERROR));
            }
            if (files.stream().anyMatch(file -> fieldIsMissing(file, "size"))) {
                toReturn.add(new MetadataCheck("File size is missing", ERROR));
            }
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkSupportingDocs(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val supportingDocs = parsed.read(
                "$.supportingDocs[*].['name','content']",
                typeRefStringString
        );

        if (supportingDocs.size() == 0) {
            toReturn.add(new MetadataCheck("Supporting documentation is empty", ERROR));
        }

        if (supportingDocs.size() >= 1 ) {
            if (supportingDocs.stream().anyMatch(supportingDoc -> fieldIsMissing(supportingDoc, "name"))) {
                toReturn.add(new MetadataCheck("Supporting document name is missing", ERROR));
            } else {
                if (supportingDocs.stream().anyMatch(supportingDoc -> supportingDoc.get("name").contains(" "))) {
                    toReturn.add(new MetadataCheck("Supporting document name should not contain any spaces", ERROR));
                }
                if (supportingDocs.stream().noneMatch(supportingDoc -> supportingDoc.get("name").matches("^[\\w-_\\.]*$"))) {
                    toReturn.add(new MetadataCheck("Supporting document name should only consist of alphanumeric characters, underscore and hyphens", ERROR));
                }
            }

            if (supportingDocs.stream().anyMatch(supportingDoc -> fieldIsMissing(supportingDoc, "content"))) {
                toReturn.add(new MetadataCheck("Supporting document content is missing", ERROR));
            }
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    private Optional<List<MetadataCheck>> checkEmail(List<Map<String, String>> maps, String errorMessage) {
        List<MetadataCheck> toReturn = new ArrayList<>();
        maps.stream()
                .filter(map -> map.containsKey("email"))
                .map(map -> map.get("email"))
                .filter(email -> isInvalidEmail(email))
                .forEach(email -> toReturn.add(new MetadataCheck(format(errorMessage, email), ERROR)));
        return Optional.of(toReturn);
    }

    private boolean isInvalidEmail(String email) {
        if (EMAIL_PATTERN.matcher(email).matches()
                && !email.equals("enquiries@ceh.ac.uk")
                && !email.equals("info@eidc.ac.uk")) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isQualifyingDocument(DocumentContext parsedMeta) {
        val docType = parsedMeta.read("$.documentType", String.class);
        return docType != null;
    }

    private boolean stringIsMissing(String key) {
        return key == null || key.isEmpty();
    }

    private boolean fieldIsMissing(Map<String, String> map, String key) {
        return map == null
                || !map.containsKey(key)
                || map.get(key) == null
                || map.get(key).isEmpty();
    }
}
