package uk.ac.ceh.gateway.catalogue.quality;

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
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;

import java.math.BigDecimal;
import java.util.*;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.DocumentTypes.GEMINI;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.*;

@SuppressWarnings("rawtypes")
@Slf4j
@ToString
@Service
public class MetadataQualityService {
    private final DocumentReader documentReader;
    private final Configuration config;
    private final Set<String> validResourceTypes = ImmutableSet.of(
            "application",
            "dataset",
            "nonGeographicDataset",
            "service"
            );
    private final Set<String> allowedEmails = ImmutableSet.of(
            "enquiries@ceh.ac.uk",
            "info@eidc.ac.uk"
            );
    private final TypeRef<List<Map<String, String>>> typeRefStringString = new TypeRef<>() {
    };

    public MetadataQualityService(
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
                    documentReader.read(id, "raw"),
                    config
                    );
            val parsedMeta = JsonPath.parse(
                    documentReader.read(id, "meta"),
                    config
                    );

            if (isQualifyingDocument(parsedDoc, parsedMeta)) {
                val checks = new ArrayList<MetadataCheck>();
                checkBasics(parsedDoc).ifPresent(checks::addAll);
                checkPublishedData(parsedDoc).ifPresent(checks::addAll);
                checkSpatialDataset(parsedDoc).ifPresent(checks::addAll);
                checkService(parsedDoc).ifPresent(checks::addAll);
                checkNonGeographicDatasets(parsedDoc).ifPresent(checks::addAll);
                checkNercSignpost(parsedDoc).ifPresent(checks::addAll);
                checkPublicationDate(parsedDoc, parsedMeta).ifPresent(checks::add);
                checkTemporalExtents(parsedDoc).ifPresent(checks::addAll);
                checkDownloadAndOrderLinks(parsedDoc).ifPresent(checks::addAll);
                checkEmbargo(parsedDoc).ifPresent(checks::addAll);
                return new Results(checks, id);
            } else {
                return new Results(Collections.emptyList(), id, "Not a qualifying document type");
            }
        } catch (Exception ex) {
            log.error("Error - could not check " + id, ex);
            return new Results(Collections.emptyList(), id, "Error - could not check this document");
        }
    }

    private boolean isQualifyingDocument(DocumentContext parsedDoc, DocumentContext parsedMeta) {
        val docType = parsedMeta.read("$.documentType", String.class);
        return docType != null
            && docType.equals(GEMINI)
            && isCorrectResourceType(parsedDoc);
    }

    Optional<List<MetadataCheck>> checkBasics(DocumentContext parsedDoc) {
        val requiredKeys = ImmutableSet.of("title", "description", "resourceType");
        val toReturn = new ArrayList<MetadataCheck>();
        val toCheck = parsedDoc.read(
                "$.['title','description']",
                new TypeRef<Map<String, String>>() {}
                );
        toCheck.put("resourceType", parsedDoc.read("$.resourceType.value", String.class));
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

    private Optional<MetadataCheck> checkPublicationDate(DocumentContext parsedDoc, DocumentContext parsedMeta) {
        val mapTypeRef = new TypeRef<Map<String, String>>() {};
        val state = parsedMeta.read("$.state", String.class);
        val datasetReferenceDate = parsedDoc.read("$.datasetReferenceDate", mapTypeRef);
        if ( !state.equals("draft") && fieldIsMissing(datasetReferenceDate, "publicationDate")) {
            return Optional.of(new MetadataCheck("Publication date is missing", ERROR));
        } else {
            return Optional.empty();
        }
    }

    private Optional<List<MetadataCheck>> checkTemporalExtents(DocumentContext parsedDoc) {
        if ( !notRequiredResourceTypes(parsedDoc, "application")) {
            return Optional.empty();
        }
        val toReturn = new ArrayList<MetadataCheck>();

        val temporalExtents = parsedDoc.read("$.temporalExtents[*]", typeRefStringString);
        if (temporalExtents ==  null || temporalExtents.isEmpty()) {
            toReturn.add(new MetadataCheck("Temporal extents is missing", INFO));
        }
        if (temporalExtents != null && temporalExtents.stream().anyMatch(this::beginAndEndBothEmpty)) {
            toReturn.add(new MetadataCheck("Temporal extents is empty", ERROR));
        }
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    private boolean beginAndEndBothEmpty(Map<String, String> map) {
        return fieldIsMissing(map, "begin") && fieldIsMissing(map, "end");
    }

    private boolean isCorrectResourceType(DocumentContext parsed) {
        return validResourceTypes.contains(parsed.read("$.resourceType.value", String.class));
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

    Optional<List<MetadataCheck>> checkNonGeographicDatasets(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "nonGeographicDataset")) {
            return Optional.empty();
        }
        val toReturn = new ArrayList<MetadataCheck>();
        val toCheck = parsed.read(
                "$.['boundingBoxes','spatialRepresentationTypes','spatialReferenceSystems','spatialResolutions']",
                new TypeRef<Map<String, List>>() {}
                );
        toCheck.forEach((key, value) -> {
            if ( !fieldListIsMissing(toCheck, key)) {
                toReturn.add(new MetadataCheck("The record has " + key + " but the resource type is Non-geographic dataset", ERROR));
            }
        });
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkSpatialDataset(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "dataset")) {
            return Optional.empty();
        }
        val requiredKeys = ImmutableSet.of("boundingBoxes", "spatialRepresentationTypes");
        Boolean notGEMINI = parsed.read("$.notGEMINI", boolean.class);
        val toReturn = new ArrayList<MetadataCheck>();

        checkBoundingBoxes(parsed).ifPresent(toReturn::addAll);
        val spatial = parsed.read(
                "$.['boundingBoxes','spatialRepresentationTypes']",
                new TypeRef<Map<String, List>>() {}
                );
        requiredKeys.forEach(key -> {
            if (fieldListIsMissing(spatial, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });

        if (notGEMINI ==  null || !notGEMINI) {
            checkInspireThemes(parsed).ifPresent(toReturn::add);
            checkSpatialResolutions(parsed).ifPresent(toReturn::add);
            checkSpatialReferenceSystems(parsed).ifPresent(toReturn::add);
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkService(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "service")) {
            return Optional.empty();
        }
        val requiredKeys = ImmutableSet.of("boundingBoxes","spatialReferenceSystems");
        val toReturn = new ArrayList<MetadataCheck>();
        checkTopicCategoriesService(parsed).ifPresent(toReturn::add);
        checkBoundingBoxes(parsed).ifPresent(toReturn::addAll);
        val spatial = parsed.read(
                "$.['boundingBoxes','spatialReferenceSystems']",
                new TypeRef<Map<String, List>>() {}
                );
        requiredKeys.forEach(key -> {
            if (fieldListIsMissing(spatial, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkBoundingBoxes(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val boundingBoxes = parsed.read(
                "$.boundingBoxes[*].['northBoundLatitude','southBoundLatitude','eastBoundLongitude','westBoundLongitude']",
                new TypeRef<List<Map<String, Number>>>() {}
                );

        boundingBoxes.forEach(boundingBox -> {
            boundingBox.forEach((key, value) -> {
                if (BigDecimal.valueOf(value.doubleValue()).scale() > 3) {
                    toReturn.add(new MetadataCheck( "Bounding box coordinates are too precise (max 3 decimal places)", ERROR));
                }
            });
            if (boundingBox.containsKey("northBoundLatitude") && boundingBox.containsKey("southBoundLatitude")) {
                val north = boundingBox.get("northBoundLatitude");
                val south = boundingBox.get("southBoundLatitude");
                if (north.doubleValue() < south.doubleValue()) {
                    toReturn.add(new MetadataCheck("Bounding box north boundary is smaller than the south", ERROR));
                }

                if (north.doubleValue() < -90 || north.doubleValue() > 90 ) {
                    toReturn.add(new MetadataCheck("Bounding box north boundary is out of range", ERROR));
                }

                if (south.doubleValue() < -90 || south.doubleValue() > 90 ) {
                    toReturn.add(new MetadataCheck("Bounding box south boundary is out of range", ERROR));
                }

            }
            if (boundingBox.containsKey("westBoundLongitude") && boundingBox.containsKey("eastBoundLongitude")) {
                val east = boundingBox.get("eastBoundLongitude");
                val west = boundingBox.get("westBoundLongitude");

                if (east.doubleValue() < -180 || east.doubleValue() > 180) {
                    toReturn.add(new MetadataCheck("Bounding box east boundary is out of range", ERROR));
                }

                if (west.doubleValue() < -180 || west.doubleValue() > 180 ) {
                    toReturn.add(new MetadataCheck("Bounding box west boundary is out of range", ERROR));
                }
            }
        });
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkPublishedData(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application")) {
            return Optional.empty();
        }

        val requiredKeys = ImmutableSet.of("resourceStatus", "lineage");
        val toReturn = new ArrayList<MetadataCheck>();
        val toCheck = parsed.read(
                "$.['resourceStatus', 'lineage']",
                new TypeRef<Map<String, String>>() {}
                );
        requiredKeys.forEach(key -> {
            if (fieldIsMissing(toCheck, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });
        val licences = parsed.read("$.useConstraints[*][?(@.code == 'license')]", typeRefStringString);
        if (licences == null || licences.isEmpty()) {
            toReturn.add(new MetadataCheck("Licence is missing", ERROR));
        }
        if (licences != null && licences.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE licence", ERROR));
        }

        checkAuthors(parsed).ifPresent(toReturn::addAll);
        checkKeywords(parsed).ifPresent(toReturn::add);
        checkTopicCategories(parsed).ifPresent(toReturn::add);
        checkDataFormat(parsed).ifPresent(toReturn::add);
        checkRelatedRecords(parsed).ifPresent(toReturn::add);
        checkPointOfContact(parsed).ifPresent(toReturn::addAll);
        checkCustodian(parsed).ifPresent(toReturn::addAll);
        checkPublisher(parsed).ifPresent(toReturn::addAll);
        checkDistributor(parsed).ifPresent(toReturn::addAll);
        checkDistributorEIDC(parsed).ifPresent(toReturn::addAll);
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkNercSignpost(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "nercSignpost")) {
            return Optional.empty();
        }
        val toReturn = new ArrayList<MetadataCheck>();
        checkAuthors(parsed).ifPresent(toReturn::addAll);
        checkTopicCategories(parsed).ifPresent(toReturn::add);
        checkDataFormat(parsed).ifPresent(toReturn::add);
        checkPointOfContact(parsed).ifPresent(toReturn::addAll);
        checkDistributor(parsed).ifPresent(toReturn::addAll);

        val size = parsed.read(
                "$.onlineResources[*][?(@.function in ['information', 'search'])].function",
                List.class
                ).size();
        if (size < 1) {
            toReturn.add(new MetadataCheck("Search/information link is missing", ERROR));
        }
        if (size > 1){
            toReturn.add(new MetadataCheck("There are more than one search/information links", INFO));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkDistributor(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val distributors = parsed.read(
                "$.distributorContacts[*][?(@.role == 'distributor')].['organisationName','email']",
                typeRefStringString
                );
        val nonDistributors = parsed.read(
                "$.distributorContacts[*][?(@.role != 'distributor')].['organisationName','email']",
                typeRefStringString
                );

        checkAddress(distributors, "Distributor").ifPresent(toReturn::addAll);
        if (distributors.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE distributor", ERROR));
        }
        if (nonDistributors.size() > 0) {
            toReturn.add(new MetadataCheck("Distributor contact must have role 'distributor'", ERROR));
        }
        if (distributors.stream().anyMatch(distributor -> fieldIsMissing(distributor, "email"))) {
            toReturn.add(new MetadataCheck("Distributor's email address is missing", ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkDistributorEIDC(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val distributors = parsed.read(
                "$.distributorContacts[*][?(@.role == 'distributor')].['organisationName','email']",
                typeRefStringString
                );
        distributors.stream()
            .filter(distributor -> fieldNotEqual(distributor, "organisationName", "NERC EDS Environmental Information Data Centre"))
            .map(distributor -> distributor.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Distributor name is " + organisationName, INFO)));

        distributors.stream()
            .filter(distributor -> fieldNotEqual(distributor, "email", "info@eidc.ac.uk"))
            .map(distributor -> distributor.getOrDefault("email", "unknown"))
            .forEach(email -> toReturn.add(new MetadataCheck("Distributor's email address is " + email, INFO)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkPublisher(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val publishers = parsed.read(
                "$.responsibleParties[*][?(@.role == 'publisher')].['organisationName','email']",
                typeRefStringString
                );
        checkAddress(publishers, "Publisher").ifPresent(toReturn::addAll);
        if (publishers.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE publisher", ERROR));
        }
        if (publishers.stream().anyMatch(publisher -> fieldIsMissing(publisher, "email"))) {
            toReturn.add(new MetadataCheck("Publisher email address is missing", ERROR));
        }
        publishers.stream()
            .filter(publisher -> fieldNotEqual(publisher, "organisationName", "NERC EDS Environmental Information Data Centre") && fieldNotEqual(publisher, "organisationName", "NERC Environmental Information Data Centre"))
            .map(publisher -> publisher.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Publisher name is " + organisationName, WARNING)));

        publishers.stream()
            .filter(publisher -> fieldNotEqual(publisher, "email", "info@eidc.ac.uk"))
            .map(publisher -> publisher.getOrDefault("email", "unknown"))
            .forEach(email -> toReturn.add(new MetadataCheck("Publishers email address is " + email, INFO)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkCustodian(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val custodians = parsed.read(
                "$.responsibleParties[*][?(@.role == 'custodian')].['organisationName','email']",
                typeRefStringString
                );
        checkAddress(custodians, "Custodian").ifPresent(toReturn::addAll);
        if (custodians.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE custodian", ERROR));
        }
        if (custodians.stream().anyMatch(custodian -> fieldIsMissing(custodian, "email"))) {
            toReturn.add(new MetadataCheck("Custodian email address is missing", ERROR));
        }
        custodians.stream()
            .filter(custodian -> fieldNotEqual(custodian, "organisationName", "NERC EDS Environmental Information Data Centre"))
            .map(custodian -> custodian.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Custodian name is " + organisationName, INFO)));

        custodians.stream()
            .filter(custodian -> fieldNotEqual(custodian, "email", "info@eidc.ac.uk"))
            .map(custodian -> custodian.getOrDefault("email", "unknown"))
            .forEach(email -> toReturn.add(new MetadataCheck("Custodian email address is " + email, INFO)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkPointOfContact(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val pocs = parsed.read(
                "$.responsibleParties[*][?(@.role == 'pointOfContact')].['organisationName','individualName','email']",
                typeRefStringString
                );
        if (pocs.size() == 0 ) {
            toReturn.add(new MetadataCheck("Point of contact is missing", ERROR));
        }
        if (pocs.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE point of contact", INFO));
        }
        if (pocs.stream().anyMatch(poc -> fieldIsMissing(poc, "email"))) {
            toReturn.add(new MetadataCheck("Point of contact email address is missing", ERROR));
        }
        if (pocs.stream().anyMatch(poc -> fieldIsMissing(poc, "individualName"))) {
            toReturn.add(new MetadataCheck("Point of contact name is missing", INFO));
        }
        if (pocs.stream().anyMatch(poc -> fieldIsMissing(poc, "organisationName"))) {
            toReturn.add(new MetadataCheck("Point of contact organisation name is missing", ERROR));
        }

        pocs.stream()
            .filter(poc -> poc.containsKey("email"))
            .map(poc -> poc.get("email"))
            .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk"))
            .forEach(email -> toReturn.add(new MetadataCheck(format("Point of contact's email address is %s", email), ERROR)));

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<MetadataCheck> checkInspireThemes(DocumentContext parsed) {
        val inspireThemes = parsed.read(
                "$.inspireThemes[*]",
                typeRefStringString
                );
        if (inspireThemes.isEmpty()) {
            return Optional.of(new MetadataCheck("INSPIRE Theme is missing", ERROR));
        }
        if (inspireThemes.stream().anyMatch(inspireTheme -> fieldIsMissing(inspireTheme, "theme"))) {
            return Optional.of(new MetadataCheck("INSPIRE Theme is empty", ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> checkSpatialResolutions(DocumentContext parsed) {
        val spatialResolutions = parsed.read(
                "$.spatialResolutions[*]",
                typeRefStringString
                );
        if (spatialResolutions.isEmpty()) {
            return Optional.of(new MetadataCheck("Spatial resolutions is missing", WARNING));
        }
        if (spatialResolutions.stream().anyMatch(spatialResolution -> fieldIsMissing(spatialResolution, "distance"))) {
            return Optional.of(new MetadataCheck("Spatial resolutions is empty", ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> checkSpatialReferenceSystems(DocumentContext parsed) {
        val spatialReferenceSystems = parsed.read(
                "$.spatialReferenceSystems[*]",
                typeRefStringString
                );
        if (spatialReferenceSystems.isEmpty()) {
            return Optional.of(new MetadataCheck("Spatial reference systems are missing", ERROR));
        }
        if (spatialReferenceSystems.stream().anyMatch(spatialReferenceSystem -> fieldIsMissing(spatialReferenceSystem, "code"))) {
            return Optional.of(new MetadataCheck("Spatial reference system is empty", ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> checkDataFormat(DocumentContext parsed) {
        val dataFormats = parsed.read(
                "$.distributionFormats[*]",
                typeRefStringString
                );
        if (dataFormats.isEmpty()) {
            return Optional.of(new MetadataCheck("Data format is missing", ERROR));
        }
        if (dataFormats.stream().anyMatch(format -> fieldIsMissing(format, "name"))) {
            return Optional.of(new MetadataCheck("Format name is empty", ERROR));
        }
        if (dataFormats.stream().anyMatch(format -> fieldIsMissing(format, "version"))) {
            return Optional.of(new MetadataCheck("Format version is empty", ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> checkRelatedRecords(DocumentContext parsed) {
        val relatedRecords = parsed.read(
                "$.relatedRecords[*]",
                typeRefStringString
                );
        if (relatedRecords.stream().anyMatch(relatedRecord -> fieldIsMissing(relatedRecord, "rel"))) {
            return Optional.of(new MetadataCheck("Related record is incomplete", ERROR));
        }
        if (relatedRecords.stream().anyMatch(relatedRecord -> fieldIsMissing(relatedRecord, "href"))) {
            return Optional.of(new MetadataCheck("Related record is incomplete", ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> checkKeywords(DocumentContext parsed) {
        val keywordsInstrument = parsed.read("$.keywordsInstrument[*]", typeRefStringString);
        val keywordsObservedProperty = parsed.read("$.keywordsObservedProperty[*]", typeRefStringString);
        val keywordsPlace = parsed.read("$.keywordsPlace[*]", typeRefStringString);
        val keywordsProject = parsed.read("$.keywordsProject[*]", typeRefStringString);
        val keywordsTheme = parsed.read("$.keywordTheme[*]", typeRefStringString);
        val keywordsOther = parsed.read("$.keywordsOther[*]", typeRefStringString);
        val allKeywords = new ArrayList<Map<String, String>>();
        allKeywords.addAll(keywordsInstrument);
        allKeywords.addAll(keywordsObservedProperty);
        allKeywords.addAll(keywordsPlace);
        allKeywords.addAll(keywordsProject);
        allKeywords.addAll(keywordsTheme);
        allKeywords.addAll(keywordsOther);
        if (allKeywords.isEmpty()) {
            return Optional.of(new MetadataCheck("There are no keywords", ERROR));
        }
        if (allKeywords.stream().anyMatch(keyword -> fieldIsMissing(keyword, "value"))) {
            return Optional.of(new MetadataCheck("Keyword is empty", ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> checkTopicCategories(DocumentContext parsed) {
        val topicCategories = parsed.read(
                "$.topicCategories[*]",
                typeRefStringString
                );
        if (topicCategories.isEmpty()) {
            return Optional.of(new MetadataCheck("Topic category is missing", ERROR));
        }
        if (topicCategories.stream().anyMatch(topic -> fieldIsMissing(topic, "value"))) {
            return Optional.of(new MetadataCheck("Topic category is empty", ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> checkTopicCategoriesService(DocumentContext parsed) {
        val topicCategories = parsed.read(
                "$.topicCategories[*]",
                typeRefStringString
                );
        if (topicCategories.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new MetadataCheck("Metadata contains topic categories which is incorrect for a service", ERROR));
        }
    }

    Optional<List<MetadataCheck>> checkAuthors(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val authors = parsed.read(
                "$.responsibleParties[*][?(@.role == 'author')].['individualName', 'organisationName','email']",
                typeRefStringString
                );
        if (authors.size() == 0) {
            toReturn.add(new MetadataCheck("There are no authors", INFO));
        }
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

    private boolean fieldIsMissing(Map<String, String> map, String key) {
        return map == null
            || !map.containsKey(key)
            || map.get(key) == null
            || map.get(key).isEmpty();
    }

    private boolean fieldListIsMissing(Map<String, List> map, String key) {
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

    Optional<List<MetadataCheck>> checkDownloadAndOrderLinks(DocumentContext parsed) {
        if (!resourceStatusIsAvailable(parsed) || notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application")) {
            return Optional.empty();
        }

        val toReturn = new ArrayList<MetadataCheck>();
        val orders = parsed.read(
                "$.onlineResources[*][?(@.function == 'order')]",
                typeRefStringString
                );
        val downloads = parsed.read(
                "$.onlineResources[*][?(@.function == 'download')]",
                typeRefStringString
                );
        val totalOrdersAndDownloads = orders.size() + downloads.size();

        if (totalOrdersAndDownloads == 0) {
            toReturn.add(new MetadataCheck("There are no orders/downloads", WARNING));
        } else if (totalOrdersAndDownloads > 1) {
            toReturn.add(new MetadataCheck("There are multiple orders/downloads", INFO));
        }

        if(orders.stream()
                .anyMatch(order -> fieldNotStartingWith(order, "url", "https://order-eidc.ceh.ac.uk/resources"))) {
            toReturn.add(new MetadataCheck("Orders do not have a valid EIDC url", INFO));
                }
        if(downloads.stream()
                .anyMatch(order -> fieldNotStartingWith(order, "url", "https://catalogue.ceh.ac.uk/datastore/eidchub/") && fieldNotStartingWith(order, "url", "https://data-package.ceh.ac.uk/data"))) {
            toReturn.add(new MetadataCheck("Downloads do not have a valid EIDC url", INFO));
                }
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkEmbargo(DocumentContext parsed) {
        if (!resourceStatusIsEmbargoed(parsed) || notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application")) {
            return Optional.empty();
        }

        val toReturn = new ArrayList<MetadataCheck>();
        val orders = parsed.read(
                "$.onlineResources[*][?(@.function == 'order')]",
                typeRefStringString
                );
        val downloads = parsed.read(
                "$.onlineResources[*][?(@.function == 'download')]",
                typeRefStringString
                );
        val totalOrdersAndDownloads = orders.size() + downloads.size();

        if (totalOrdersAndDownloads > 0) {
            toReturn.add(new MetadataCheck("This resource is embargoed but it contains orders/downloads", ERROR));
        }

        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    private boolean fieldNotStartingWith(Map<String, String> map, String key, String value) {
        return !map.containsKey(key)
            || map.get(key) == null
            || !map.get(key).startsWith(value);
    }

    boolean resourceStatusIsAvailable(DocumentContext parsed) {
        val resourceStatus = parsed.read("$.resourceStatus", String.class);
        return resourceStatus != null && resourceStatus.equals("Available");
    }

    boolean resourceStatusIsEmbargoed(DocumentContext parsed) {
        val resourceStatus = parsed.read("$.resourceStatus", String.class);
        return resourceStatus != null && resourceStatus.equals("Embargoed");
    }

    boolean descriptionTooShort(DocumentContext parsed) {
        val description = parsed.read("$.description", String.class);
        return description.length()<5;
    }

    private boolean notRequiredResourceTypes(DocumentContext parsed, String... resourceTypes) {
        val testPath = new StringBuilder("$.resourceType[?(@.value in [");
        for (int i = 0; i < resourceTypes.length; i++) {
            if (i > 0) {
                testPath.append(",");
            }
            testPath.append("'").append(resourceTypes[i]).append("'");
        }
        testPath.append("])].value");
        return parsed.read(testPath.toString(), List.class).isEmpty();
    }
}
