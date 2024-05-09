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
import java.util.stream.Stream;

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
    private final TypeRef<List<Map<String, String>>> typeRefStringString = new TypeRef<>() {};

    public MetadataQualityService(
            @NonNull DocumentReader documentReader,
            @NonNull ObjectMapper objectMapper
    ) {
        this.documentReader = documentReader;
        this.config = Configuration.defaultConfiguration()
            .jsonProvider(new JacksonJsonProvider(objectMapper))
            .mappingProvider(new JacksonMappingProvider(objectMapper))
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS);
        log.info("Creating");
    }

    @SneakyThrows
    public Results check(String id) {
        log.debug("Checking {}", id);

        try {
            val parsedDoc = JsonPath.parse(documentReader.read(id, "raw"), config);
            val parsedMeta = JsonPath.parse(documentReader.read(id, "meta"), config);

            if (isQualifyingDocument(parsedDoc, parsedMeta)) {
                val checks = Stream.of(
                    checkBasics(parsedDoc).stream(),
                    checkPublishedData(parsedDoc).stream(),
                    checkSpatialDataset(parsedDoc).stream(),
                    checkService(parsedDoc).stream(),
                    checkNonGeographicDatasets(parsedDoc).stream(),
                    checkNercSignpost(parsedDoc).stream(),
                    checkPublicationDate(parsedDoc, parsedMeta).stream(),
                    checkTemporalExtents(parsedDoc).stream(),
                    checkDownloadAndOrderLinks(parsedDoc).stream(),
                    checkEmbargo(parsedDoc).stream()
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

    private boolean isQualifyingDocument(DocumentContext parsedDoc, DocumentContext parsedMeta) {
        val docType = parsedMeta.read("$.documentType", String.class);
        return docType != null
            && docType.equals(GEMINI)
            && isCorrectResourceType(parsedDoc);
    }

    List<MetadataCheck> checkBasics(DocumentContext parsedDoc) {
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

        val description = toCheck.get("description");
        if (description == null || description.length() < 100) {
            toReturn.add(new MetadataCheck("Description is too short (minimum 100 characters)", ERROR));
        }

        return toReturn;
    }

    private List<MetadataCheck> checkPublicationDate(DocumentContext parsedDoc, DocumentContext parsedMeta) {
        val mapTypeRef = new TypeRef<Map<String, String>>() {};
        val state = parsedMeta.read("$.state", String.class);
        val datasetReferenceDate = parsedDoc.read("$.datasetReferenceDate", mapTypeRef);
        if (!state.equals("draft") && fieldIsMissing(datasetReferenceDate, "publicationDate")) {
            return Collections.singletonList(new MetadataCheck("Publication date is missing", ERROR));
        }
        return Collections.emptyList();
    }

    private List<MetadataCheck> checkTemporalExtents(DocumentContext parsedDoc) {
        if (!notRequiredResourceTypes(parsedDoc, "application")) {
            return Collections.emptyList();
        }

        val temporalExtents = parsedDoc.read("$.temporalExtents[*]", typeRefStringString);
        if (temporalExtents == null || temporalExtents.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("Temporal extents are missing", INFO));
        }

        if (temporalExtents.stream().anyMatch(this::beginAndEndBothEmpty)) {
            return Collections.singletonList(new MetadataCheck("Temporal extent is empty", ERROR));
        }

        return Collections.emptyList();
    }

    private boolean beginAndEndBothEmpty(Map<String, String> map) {
        return fieldIsMissing(map, "begin") && fieldIsMissing(map, "end");
    }

    private boolean isCorrectResourceType(DocumentContext parsed) {
        return validResourceTypes.contains(parsed.read("$.resourceType.value", String.class));
    }

    List<MetadataCheck> checkAddress(List<Map<String, String>> addresses, String element) {
        if (addresses == null || addresses.isEmpty()) {
            return Collections.singletonList(new MetadataCheck(element + " is missing", ERROR));
        }
        if (addresses.stream().anyMatch(map -> fieldIsMissing(map, "organisationName"))) {
            return Collections.singletonList(new MetadataCheck(element + " organisation name is missing", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkNonGeographicDatasets(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (notRequiredResourceTypes(parsed, "nonGeographicDataset")) {
            return toReturn;
        }
        val toCheck = parsed.read(
                "$.['boundingBoxes','spatialRepresentationTypes','spatialReferenceSystems','spatialResolutions']",
                new TypeRef<Map<String, List>>() {}
        );
        toCheck.forEach((key, value) -> {
            if (!fieldListIsMissing(toCheck, key)) {
                toReturn.add(new MetadataCheck("The record has " + key + " but the resource type is Non-geographic dataset", ERROR));
            }
        });
        return toReturn;
    }

    List<MetadataCheck> checkSpatialDataset(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (notRequiredResourceTypes(parsed, "dataset")) {
            return toReturn;
        }
        val requiredKeys = ImmutableSet.of("boundingBoxes", "spatialRepresentationTypes");
        Boolean notGEMINI = parsed.read("$.notGEMINI", boolean.class);

        toReturn.addAll(checkBoundingBoxes(parsed));
        val spatial = parsed.read(
                "$.['boundingBoxes','spatialRepresentationTypes']",
                new TypeRef<Map<String, List>>() {}
        );
        requiredKeys.forEach(key -> {
            if (fieldListIsMissing(spatial, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });

        if (notGEMINI == null || !notGEMINI) {
            toReturn.addAll(
                Stream.of(
                    checkInspireThemes(parsed).stream(),
                    checkSpatialResolutions(parsed).stream(),
                    checkSpatialReferenceSystems(parsed).stream()
                ).flatMap(s -> s).toList()
            );
        }

        return toReturn;
    }

    List<MetadataCheck> checkService(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (notRequiredResourceTypes(parsed, "service")) {
            return toReturn;
        }
        val requiredKeys = ImmutableSet.of("boundingBoxes","spatialReferenceSystems");
        toReturn.addAll(checkTopicCategoriesService(parsed));
        toReturn.addAll(checkBoundingBoxes(parsed));
        val spatial = parsed.read(
                "$.['boundingBoxes','spatialReferenceSystems']",
                new TypeRef<Map<String, List>>() {}
        );
        requiredKeys.forEach(key -> {
            if (fieldListIsMissing(spatial, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });
        return toReturn;
    }

    List<MetadataCheck> checkBoundingBoxes(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val boundingBoxes = parsed.read(
                "$.boundingBoxes[*].['northBoundLatitude','southBoundLatitude','eastBoundLongitude','westBoundLongitude']",
                new TypeRef<List<Map<String, Double>>>() {}
        );

        boundingBoxes.forEach(boundingBox -> {
            boundingBox.forEach((key, value) -> {
                if (BigDecimal.valueOf(value).scale() > 3) {
                    toReturn.add(new MetadataCheck("Bounding box coordinates are too precise (max 3 decimal places)", ERROR));
                }
            });
            if (boundingBox.containsKey("northBoundLatitude") && boundingBox.containsKey("southBoundLatitude")) {
                val north = boundingBox.get("northBoundLatitude");
                val south = boundingBox.get("southBoundLatitude");
                if (north < south) {
                    toReturn.add(new MetadataCheck("Bounding box north boundary is smaller than the south", ERROR));
                }

                if (north < -90 || north > 90) {
                    toReturn.add(new MetadataCheck("Bounding box north boundary is out of range", ERROR));
                }

                if (south < -90 || south > 90) {
                    toReturn.add(new MetadataCheck("Bounding box south boundary is out of range", ERROR));
                }

            }
            if (boundingBox.containsKey("westBoundLongitude") && boundingBox.containsKey("eastBoundLongitude")) {
                val east = boundingBox.get("eastBoundLongitude");
                val west = boundingBox.get("westBoundLongitude");

                if (east < -180 || east > 180) {
                    toReturn.add(new MetadataCheck("Bounding box east boundary is out of range", ERROR));
                }

                if (west < -180 || west > 180 ) {
                    toReturn.add(new MetadataCheck("Bounding box west boundary is out of range", ERROR));
                }
            }
        });

        return toReturn;
    }

    List<MetadataCheck> checkPublishedData(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application")) {
            return toReturn;
        }

        val requiredKeys = ImmutableSet.of("resourceStatus", "lineage");
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
        } else if (licences.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE licence", ERROR));
        }

        toReturn.addAll(
            Stream.of(
                checkAuthors(parsed).stream(),
                checkKeywords(parsed).stream(),
                checkTopicCategories(parsed).stream(),
                checkDataFormat(parsed).stream(),
                checkRelatedRecords(parsed).stream(),
                checkPointOfContact(parsed).stream(),
                checkCustodian(parsed).stream(),
                checkPublisher(parsed).stream(),
                checkDistributor(parsed).stream(),
                checkDistributorEIDC(parsed).stream()
            ).flatMap(s -> s).toList()
        );

        return toReturn;
    }

    List<MetadataCheck> checkNercSignpost(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (notRequiredResourceTypes(parsed, "nercSignpost")) {
            return toReturn;
        }
        toReturn.addAll(
            Stream.of(
                checkAuthors(parsed).stream(),
                checkTopicCategories(parsed).stream(),
                checkDataFormat(parsed).stream(),
                checkPointOfContact(parsed).stream(),
                checkDistributor(parsed).stream()
            ).flatMap(s -> s).toList()
        );

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

        return toReturn;
    }

    List<MetadataCheck> checkDistributor(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val distributors = parsed.read(
                "$.distributorContacts[*][?(@.role == 'distributor')].['organisationName','email']",
                typeRefStringString
        );
        val nonDistributors = parsed.read(
                "$.distributorContacts[*][?(@.role != 'distributor')].['organisationName','email']",
                typeRefStringString
        );

        toReturn.addAll(checkAddress(distributors, "Distributor"));
        if (distributors.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE distributor", ERROR));
        }
        if (nonDistributors.size() > 0) {
            toReturn.add(new MetadataCheck("Distributor contact must have role 'distributor'", ERROR));
        }
        if (distributors.stream().anyMatch(distributor -> fieldIsMissing(distributor, "email"))) {
            toReturn.add(new MetadataCheck("Distributor's email address is missing", ERROR));
        }

        return toReturn;
    }

    List<MetadataCheck> checkDistributorEIDC(DocumentContext parsed) {
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

        return toReturn;
    }

    List<MetadataCheck> checkPublisher(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val publishers = parsed.read(
                "$.responsibleParties[*][?(@.role == 'publisher')].['organisationName','email']",
                typeRefStringString
        );
        toReturn.addAll(checkAddress(publishers, "Publisher"));
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

        return toReturn;
    }

    List<MetadataCheck> checkCustodian(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val custodians = parsed.read(
                "$.responsibleParties[*][?(@.role == 'custodian')].['organisationName','email']",
                typeRefStringString
                );
        toReturn.addAll(checkAddress(custodians, "Custodian"));
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

        return toReturn;
    }

    List<MetadataCheck> checkPointOfContact(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val pocs = parsed.read(
                "$.responsibleParties[*][?(@.role == 'pointOfContact')].['organisationName','individualName','email']",
                typeRefStringString
        );
        if (pocs.isEmpty()) {
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
            .map(poc -> poc.get("email"))
            .flatMap(Stream::ofNullable)
            .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk"))
            .forEach(email -> toReturn.add(new MetadataCheck(format("Point of contact's email address is %s", email), ERROR)));

        return toReturn;
    }

    List<MetadataCheck> checkInspireThemes(DocumentContext parsed) {
        val inspireThemes = parsed.read("$.inspireThemes[*]", typeRefStringString);
        if (inspireThemes.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("INSPIRE Theme is missing", ERROR));
        }
        if (inspireThemes.stream().anyMatch(inspireTheme -> fieldIsMissing(inspireTheme, "theme"))) {
            return Collections.singletonList(new MetadataCheck("INSPIRE Theme is empty", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkSpatialResolutions(DocumentContext parsed) {
        val spatialResolutions = parsed.read("$.spatialResolutions[*]", typeRefStringString);
        if (spatialResolutions.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("Spatial resolutions is missing", WARNING));
        }
        if (spatialResolutions.stream().anyMatch(spatialResolution -> fieldIsMissing(spatialResolution, "distance"))) {
            return Collections.singletonList(new MetadataCheck("Spatial resolution is empty", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkSpatialReferenceSystems(DocumentContext parsed) {
        val spatialReferenceSystems = parsed.read("$.spatialReferenceSystems[*]", typeRefStringString);
        if (spatialReferenceSystems.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("Spatial reference systems are missing", ERROR));
        }
        if (spatialReferenceSystems.stream().anyMatch(spatialReferenceSystem -> fieldIsMissing(spatialReferenceSystem, "code"))) {
            return Collections.singletonList(new MetadataCheck("Spatial reference system is empty", ERROR));
        } else {
            return Collections.emptyList();
        }
    }

    List<MetadataCheck> checkDataFormat(DocumentContext parsed) {
        val dataFormats = parsed.read("$.distributionFormats[*]", typeRefStringString);
        if (dataFormats.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("Data format is missing", ERROR));
        }
        if (dataFormats.stream().anyMatch(format -> fieldIsMissing(format, "name"))) {
            return Collections.singletonList(new MetadataCheck("Format name is empty", ERROR));
        }
        if (dataFormats.stream().anyMatch(format -> fieldIsMissing(format, "version"))) {
            return Collections.singletonList(new MetadataCheck("Format version is empty", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkRelatedRecords(DocumentContext parsed) {
        val relatedRecords = parsed.read("$.relatedRecords[*]", typeRefStringString);
        if (relatedRecords.stream().anyMatch(
            relatedRecord -> fieldIsMissing(relatedRecord, "rel") || fieldIsMissing(relatedRecord, "href")
        )) {
            return Collections.singletonList(new MetadataCheck("Related record is incomplete", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkKeywords(DocumentContext parsed) {
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
            return Collections.singletonList(new MetadataCheck("There are no keywords", ERROR));
        }
        if (allKeywords.stream().anyMatch(keyword -> fieldIsMissing(keyword, "value"))) {
            return Collections.singletonList(new MetadataCheck("Keyword is empty", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkTopicCategories(DocumentContext parsed) {
        val topicCategories = parsed.read("$.topicCategories[*]", typeRefStringString);
        if (topicCategories.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("Topic category is missing", ERROR));
        }
        if (topicCategories.stream().anyMatch(topic -> fieldIsMissing(topic, "value"))) {
            return Collections.singletonList(new MetadataCheck("Topic category is empty", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkTopicCategoriesService(DocumentContext parsed) {
        val topicCategories = parsed.read(
                "$.topicCategories[*]",
                typeRefStringString
        );
        if (!topicCategories.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("Metadata contains topic categories which is incorrect for a service", ERROR));
        }
        return Collections.emptyList();
    }

    List<MetadataCheck> checkAuthors(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val authors = parsed.read(
                "$.responsibleParties[*][?(@.role == 'author')].['individualName', 'organisationName','email']",
                typeRefStringString
        );
        if (authors.isEmpty()) {
            toReturn.add(new MetadataCheck("There are no authors", INFO));
        }
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "individualName"))) {
            toReturn.add(new MetadataCheck("Author's name is missing", INFO));
        }
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "organisationName"))) {
            toReturn.add(new MetadataCheck("Author's affiliation (organisation name) is missing", ERROR));
        }
        authors.stream()
            .map(author -> author.get("email"))
            .flatMap(Stream::ofNullable)
            .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk") && !email.equals("info@eidc.ac.uk"))
            .forEach(email -> toReturn.add(new MetadataCheck(format("Author's email address is %s", email), ERROR)));

        return toReturn;
    }

    private boolean fieldIsMissing(Map<String, String> map, String key) {
        return map == null
            || map.get(key) == null
            || map.get(key).isBlank();
    }

    private boolean fieldListIsMissing(Map<String, List> map, String key) {
        return map == null
            || map.get(key) == null
            || map.get(key).isEmpty();
    }

    private boolean fieldNotEqual(Map<String, String> map, String key, String value) {
        return map.get(key) == null
            || !map.get(key).equals(value);
    }

    List<MetadataCheck> checkDownloadAndOrderLinks(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (!resourceStatusIsAvailable(parsed) || notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application")) {
            return toReturn;
        }

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

        if (orders.stream().anyMatch(order ->
            fieldNotStartingWith(order, "url", "https://order-eidc.ceh.ac.uk/resources")
        )) {
            toReturn.add(new MetadataCheck("Orders do not have a valid EIDC url", INFO));
        }

        if (downloads.stream().anyMatch(order ->
            fieldNotStartingWith(order, "url", "https://catalogue.ceh.ac.uk/datastore/eidchub/") && fieldNotStartingWith(order, "url", "https://data-package.ceh.ac.uk/data")
        )) {
            toReturn.add(new MetadataCheck("Downloads do not have a valid EIDC url", INFO));
        }

        return toReturn;
    }

    List<MetadataCheck> checkEmbargo(DocumentContext parsed) {
        if (!resourceStatusIsEmbargoed(parsed) || notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application")) {
            return Collections.emptyList();
        }

        val orders = parsed.read(
                "$.onlineResources[*][?(@.function == 'order')]",
                typeRefStringString
        );
        val downloads = parsed.read(
                "$.onlineResources[*][?(@.function == 'download')]",
                typeRefStringString
        );

        if (!orders.isEmpty() || !downloads.isEmpty()) {
            return Collections.singletonList(new MetadataCheck("This resource is embargoed but it contains orders/downloads", ERROR));
        }

        return Collections.emptyList();
    }

    private boolean fieldNotStartingWith(Map<String, String> map, String key, String value) {
        return map.get(key) == null
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
        return description.length() < 5;
    }

    private boolean notRequiredResourceTypes(DocumentContext parsed, String... resourceTypes) {
        val testPath = new StringBuilder("$.resourceType[?(@.value in [");
        testPath.append(String.join(",",
            Stream.of(resourceTypes).map(type -> format("'%s'", type)).toList()
        ));
        testPath.append("])].value");
        return parsed.read(testPath.toString(), List.class).isEmpty();
    }
}
