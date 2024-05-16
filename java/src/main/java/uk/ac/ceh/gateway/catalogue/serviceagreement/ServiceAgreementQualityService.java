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
import java.util.stream.Stream;
import java.util.stream.Collectors;

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
    private final ImmutableSet<String> mandatoryContentTypes = ImmutableSet.of("generationMethods", "natureUnits", "qc", "dataStructure");

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
                val checks = Stream.of(
                    checkBasics(parsedDoc).stream(),
                    checkDiscoveryMetadata(parsedDoc).stream(),
                    checkAuthors(parsedDoc).stream(),
                    checkSupportingDocs(parsedDoc).stream(),
                    checkDepositorContactDetails(parsedDoc).stream(),
                    checkOwnerOfIpr(parsedDoc).stream(),
                    checkFiles(parsedDoc).stream()
                ).flatMap(s -> s).toList();
                return new Results(checks, id);
            } else {
                return new Results(Collections.emptyList(), id, "Not a qualifying document type");
            }
        } catch (Exception ex) {
            log.error("Error - could not check " + id, ex);
            return new Results(Collections.emptyList(), id, "Error - could not check this document");
        }
    }

    List<MetadataCheck> checkBasics(DocumentContext parsedDoc) {
        val requiredKeys = Map.ofEntries(
            Map.entry("Title", "title"),
            Map.entry("Depositor contact details", "depositorContactDetails"),
            Map.entry("EIDC name", "eidcName"),
            Map.entry("Transfer method", "transferMethod"),
            Map.entry("Deposit reference", "depositReference"),
            Map.entry("ISO 19115 topic categories keywords", "topicCategories"),
            Map.entry("Science topic keywords", "keywordsTheme"),
            Map.entry("Observed properties keywords", "keywordsObservedProperty"),
            Map.entry("Places keywords", "keywordsPlace"),
            Map.entry("Projects keywords", "keywordsProject"),
            Map.entry("Instruments keywords", "keywordsInstrument"),
            Map.entry("Other keywords", "keywordsOther"));

        val toReturn = new ArrayList<MetadataCheck>();
        val toCheck = parsedDoc.read(
                "$.['title', 'depositorContactDetails', 'eidcName', 'transferMethod', 'depositReference', 'topicCategories', 'keywordsTheme', 'keywordsObservedProperty', 'keywordsPlace', 'keywordsProject', 'keywordsInstrument', 'keywordsOther']",
                new TypeRef<Map<String, String>>() {}
        );

        for (var entry : requiredKeys.entrySet()) {
            if (fieldIsMissing(toCheck, entry.getValue())) {
                toReturn.add(new MetadataCheck(entry.getKey() + " is missing", ERROR));
            }
        };

        val depositReference = parsedDoc.read("$.depositReference", String.class);
        if (stringIsMissing(depositReference) || !depositReference.trim().matches("^"+jiraPrefix+"\\d{1,9}$")) {
            toReturn.add(new MetadataCheck("Deposit reference must be present and must match "+jiraPrefix+"XXXX", ERROR));
        }

        return toReturn;
    }

    List<MetadataCheck> checkDiscoveryMetadata(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val boundingBoxes = parsed.read(
            "$.boundingBoxes[*]",
            typeRefStringString
        );

        if (boundingBoxes.isEmpty()) {
            toReturn.add(new MetadataCheck("Area of study not included", INFO));
        }

        if (stringIsMissing(parsed.read("$.lineage", String.class))) {
            toReturn.add(new MetadataCheck("Lineage is incomplete", ERROR));
        }

        val description = parsed.read("$.description", String.class);
        if (stringIsMissing(description) || description.length() < 100)  {
            toReturn.add(new MetadataCheck("Description is incomplete (minimum 100 characters)", ERROR));
        }

        return toReturn;
    }

    List<MetadataCheck> checkAuthors(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val authors = parsed.read(
                "$.authors[*][?(@.role == 'author')].['individualName', 'organisationName','email']",
                typeRefStringString
        );

        if (authors.isEmpty()) {
            toReturn.add(new MetadataCheck("There are no authors", ERROR));
        }

        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "individualName"))) {
            toReturn.add(new MetadataCheck("Author's name is missing", ERROR));
        }

        if (authors.stream().anyMatch(author -> !AUTHOR_PATTERN.matcher(author.get("individualName")).matches())) {
            toReturn.add(new MetadataCheck("Author name format incorrect", ERROR));
        }

        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "organisationName"))) {
            toReturn.add(new MetadataCheck("Author's affiliation (organisation name) is missing", ERROR));
        }

        toReturn.addAll(checkEmail(authors, "Author's email address is incorrect (%s)"));

        return toReturn;
    }

    List<MetadataCheck> checkOwnerOfIpr(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val owners = parsed.read(
                "$.ownersOfIpr[*].['individualName', 'organisationName','email']",
                typeRefStringString
        );

        if (owners.isEmpty()) {
            toReturn.add(new MetadataCheck("There are no IPR owners", ERROR));
        }

        if (owners.stream().anyMatch(owner -> fieldIsMissing(owner, "organisationName"))) {
            toReturn.add(new MetadataCheck("IPR owner's affiliation (organisation name) is missing", ERROR));
        }

        return toReturn;
    }

    List<MetadataCheck> checkDepositorContactDetails(DocumentContext parsed) {

        val toReturn = new ArrayList<MetadataCheck>();
        val depositorContactDetails = parsed.read("$.depositorContactDetails", String.class);

        if (depositorContactDetails == null || depositorContactDetails.isBlank()) {
            toReturn.add(new MetadataCheck("Depositor's email address is missing", ERROR));
        }

        if (isInvalidEmail(depositorContactDetails)) {
            toReturn.add(new MetadataCheck(format("Depositor's email address is incorrect  (%s)", depositorContactDetails), ERROR));
        }

        return toReturn;
    }

    List<MetadataCheck> checkFiles(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val fileNamingConvention = parsed.read("$.fileNamingConvention", String.class);
        val fileNumber = parsed.read("$.fileNumber", String.class);
        val files = parsed.read(
                "$.files[*].['name','format', 'size']",
                typeRefStringString
        );

        if (files.isEmpty() && stringIsMissing(fileNamingConvention)) {
            toReturn.add(new MetadataCheck("You must describe the files to be deposited (either a list of files or a naming convention)", ERROR));
        }

        if (stringIsMissing(fileNumber)) {
            toReturn.add(new MetadataCheck("Number of files to be deposited is missing", ERROR));
        }

        files.stream().forEach(file -> {
            if (fieldIsMissing(file, "name")) {
                toReturn.add(new MetadataCheck("File name is missing", ERROR));
            } else if (!file.get("name").matches("^[\\w\\-\\_\\.]*$")) {
                toReturn.add(new MetadataCheck("File names should only consist of alphanumeric characters, underscores, dots and hyphens", ERROR));
            }

            if (fieldIsMissing(file, "format")) {
                toReturn.add(new MetadataCheck("File format is missing", ERROR));
            }

            if (fieldIsMissing(file, "size")) {
                toReturn.add(new MetadataCheck("File size is missing", ERROR));
            }
        });

        return toReturn;
    }

    List<MetadataCheck> checkSupportingDocs(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();

        val supportingDocs = parsed.read(
                "$.supportingDocs[*].['name', 'format', 'content']",
                new TypeRef<List<SupportingDoc>>(){}
        );

        if (supportingDocs.isEmpty()) {
            toReturn.add(new MetadataCheck("Supporting documentation is empty", ERROR));
        }

        supportingDocs.stream().forEach(supportingDoc -> {
            if (supportingDoc.getName() == null) {
                toReturn.add(new MetadataCheck("Supporting document name is missing", ERROR));
            } else if (!supportingDoc.getName().matches("^[\\w-]+$")) {
                toReturn.add(new MetadataCheck("Supporting document name should consist of alphanumeric characters, underscore and hyphens", ERROR));
            }

            if (supportingDoc.getFormat() == null) {
                toReturn.add(new MetadataCheck("Supporting document format is missing", ERROR));
            } else if (!supportingDoc.getFormat().matches("^\\p{Alnum}+$")) {
                toReturn.add(new MetadataCheck("Supporting document format should consist of alphanumeric characters", ERROR));
            }

            if (supportingDoc.getContent() == null) {
                toReturn.add(new MetadataCheck("Supporting document content is missing", ERROR));
            }
        });

        val allContentTypes = supportingDocs.stream()
            .map(SupportingDoc::getContent)
            .flatMap(Stream::ofNullable)
            .flatMap(List::stream)
            .collect(Collectors.toSet());

        if (!allContentTypes.containsAll(mandatoryContentTypes)) {
            toReturn.add(new MetadataCheck("Supporting documents do not cover all mandatory fields", ERROR));
        }

        return toReturn;
    }

    private List<MetadataCheck> checkEmail(List<Map<String, String>> maps, String errorMessage) {
        return maps.stream()
            .map(map -> map.get("email"))
            .flatMap(Stream::ofNullable)
            .filter(this::isInvalidEmail)
            .map(email -> new MetadataCheck(format(errorMessage, email), ERROR))
            .toList();
    }

    private boolean isInvalidEmail(String email) {
        return !EMAIL_PATTERN.matcher(email).matches()
            || email.equals("enquiries@ceh.ac.uk")
            || email.equals("info@eidc.ac.uk");
    }

    private boolean isQualifyingDocument(DocumentContext parsedMeta) {
        val docType = parsedMeta.read("$.documentType", String.class);
        return docType != null;
    }

    private boolean stringIsMissing(String key) {
        return key == null || key.isBlank();
    }

    private boolean fieldIsMissing(Map<String, String> map, String key) {
        return map == null
            || map.get(key) == null
            || map.get(key).isBlank();
    }
}
