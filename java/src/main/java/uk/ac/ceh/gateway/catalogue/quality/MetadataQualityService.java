package uk.ac.ceh.gateway.catalogue.quality;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig;
import uk.ac.ceh.gateway.catalogue.services.DocumentReader;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Severity.ERROR;
import static uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Severity.WARNING;
import static uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Severity.INFO;

@Slf4j
@SuppressWarnings({"unused", "WeakerAccess"})
@Service
public class MetadataQualityService {
    private final DocumentReader documentReader;
    private final Configuration config;
    private final Set<String> validResourceTypes = ImmutableSet.of(
        "application",
        "dataset",
        "nonGeographicDataset",
        "service",
        "signpost"
    );
    private final Set<String> allowedEmails = ImmutableSet.of(
        "enquiries@ceh.ac.uk",
        "eidc@ceh.ac.uk"
    );
    private final TypeRef<List<Map<String, String>>> typeRefStringString = new TypeRef<List<Map<String, String>>>() {};

    @Autowired
    public MetadataQualityService(@NonNull DocumentReader documentReader, @NonNull ObjectMapper objectMapper) {
        this.documentReader = documentReader;
        this.config = Configuration.defaultConfiguration()
            .jsonProvider(new JacksonJsonProvider(objectMapper))
            .mappingProvider(new JacksonMappingProvider(objectMapper))
            .addOptions(
                Option.DEFAULT_PATH_LEAF_TO_NULL,
                Option.SUPPRESS_EXCEPTIONS
            );
    }

    @SneakyThrows
    public Results check(String id) {
        log.debug("Checking {}", id);

        try {
            val parsedDoc = JsonPath.parse(
                this.documentReader.read(id, "raw"),
                this.config
            );
            val parsedMeta = JsonPath.parse(
                this.documentReader.read(id, "meta"),
                this.config
            );

            if (isQualifyingDocument(parsedDoc, parsedMeta)) {
                val checks = new ArrayList<MetadataCheck>();
                checkBasics(parsedDoc).ifPresent(checks::addAll);
                checkPublicationDate(parsedDoc, parsedMeta).ifPresent(checks::add);
                checkTemporalExtents(parsedDoc).ifPresent(checks::addAll);
                checkNonGeographicDatasets(parsedDoc).ifPresent(checks::addAll);
                checkSignpost(parsedDoc).ifPresent(checks::add);
                checkDataset(parsedDoc).ifPresent(checks::addAll);
                checkService(parsedDoc).ifPresent(checks::addAll);
                checkStuff(parsedDoc).ifPresent(checks::addAll);
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
            && docType.equals(CatalogueServiceConfig.GEMINI_DOCUMENT)
            && isCorrectResourceType(parsedDoc);
    }

    Optional<List<MetadataCheck>> checkBasics(DocumentContext parsedDoc) {
        val requiredKeys = ImmutableSet.of("title", "description", "lineage", "resourceStatus", "resourceType");
        val toReturn = new ArrayList<MetadataCheck>();
        val toCheck = parsedDoc.read(
            "$.['title','description','lineage','resourceStatus']",
            new TypeRef<Map<String, String>>() {}
            );
        toCheck.put("resourceType", parsedDoc.read("$.resourceType.value", String.class));
        requiredKeys.forEach(key -> {
            if (fieldIsMissing(toCheck, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });
        val licences = parsedDoc.read("$.useConstraints[*][?(@.code == 'license')]", typeRefStringString);
        if (licences == null || licences.isEmpty()) {
            toReturn.add(new MetadataCheck("Licence is missing", ERROR));
        }
        if (licences != null && licences.size() > 1) {
            toReturn.add(new MetadataCheck("There should be only ONE licence", ERROR));
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

    Optional<MetadataCheck> checkSignpost(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "signpost")) {
            return Optional.empty();
        }
        val size = parsed.read(
            "$.onlineResources[*][?(@.function in ['information', 'search'])].function",
            List.class
        ).size();
        if (size < 1) {
            return Optional.of(new MetadataCheck("Search/information link is missing", ERROR));
        } else if (size == 1) {
            return Optional.empty();
        } else {
            return Optional.of(new MetadataCheck("There are more than one search/information links", INFO));
        }
    }

    Optional<List<MetadataCheck>> checkDataset(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "dataset")) {
            return Optional.empty();
        }
        val requiredKeys = ImmutableSet.of("boundingBoxes", "spatialRepresentationTypes", "spatialReferenceSystems");
        val toReturn = new ArrayList<MetadataCheck>();
        checkInspireTheme(parsed).ifPresent(toReturn::addAll);
        checkBoundingBoxes(parsed).ifPresent(toReturn::addAll);
        val spatial = parsed.read(
            "$.['boundingBoxes','spatialRepresentationTypes','spatialReferenceSystems','spatialResolutions']",
            new TypeRef<Map<String, List>>() {}
            );
        requiredKeys.forEach(key -> {
            if (fieldListIsMissing(spatial, key)) {
                toReturn.add(new MetadataCheck(key + " is missing", ERROR));
            }
        });
        if (fieldListIsMissing(spatial, "spatialResolutions")) {
            toReturn.add(new MetadataCheck("Spatial resolution is missing", INFO));
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

    Optional<List<MetadataCheck>> checkInspireTheme(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (parsed.read("$.descriptiveKeywords[*][?(@.type == 'INSPIRE Theme')].type", List.class).isEmpty()) {
            toReturn.add(new MetadataCheck("INSPIRE theme is missing", WARNING));
            return Optional.of(toReturn);
        }
        val keywords = parsed.read("$.descriptiveKeywords[*][?(@.type == 'INSPIRE Theme')].keywords[*]", typeRefStringString);
        if (keywords.size() == 0) {
            toReturn.add(new MetadataCheck("INSPIRE theme is empty", ERROR));
            return Optional.of(toReturn);
        }
        if (keywords.stream().anyMatch(map -> fieldNotStartingWith(map, "uri", "http://inspire.ec.europa.eu/theme/"))) {
            toReturn.add(new MetadataCheck("INSPIRE theme does not have correct URI", ERROR));
        }
        if (keywords.stream().anyMatch(map -> fieldIsMissing(map, "uri"))) {
            toReturn.add(new MetadataCheck("INSPIRE theme does not have a URI", ERROR));
        }
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkBoundingBoxes(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val boundingBoxes = parsed.read(
            "$.boundingBoxes[*]",
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
                    toReturn.add(new MetadataCheck("Bounding box northern boundary is smaller than the southern", ERROR));
                }
            }
            if (boundingBox.containsKey("westBoundLongitude") && boundingBox.containsKey("eastBoundLongitude")) {
                val east = boundingBox.get("eastBoundLongitude");
                val west = boundingBox.get("westBoundLongitude");
                if (east.doubleValue() < west.doubleValue()) {
                    toReturn.add(new MetadataCheck("Bounding box east boundary is smaller than the western", ERROR));
                }
            }
        });
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkStuff(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application", "signpost")) {
            return Optional.empty();
        }
        val toReturn = new ArrayList<MetadataCheck>();
        checkAuthors(parsed).ifPresent(toReturn::addAll);
        checkTopicCategories(parsed).ifPresent(toReturn::add);
        checkDataFormat(parsed).ifPresent(toReturn::add);
        checkPointOfContact(parsed).ifPresent(toReturn::addAll);
        checkCustodian(parsed).ifPresent(toReturn::addAll);
        checkPublisher(parsed).ifPresent(toReturn::addAll);
        checkDistributor(parsed).ifPresent(toReturn::addAll);
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
        distributors.stream()
            .filter(distributor -> fieldNotEqual(distributor, "organisationName", "Environmental Information Data Centre"))
            .map(distributor -> distributor.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Distributor name is " + organisationName, INFO)));
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

        if (notRequiredResourceTypes(parsed, "signpost")) {
             distributors.stream()
            .filter(distributor -> fieldNotEqual(distributor, "email", "eidc@ceh.ac.uk"))
            .map(distributor -> distributor.getOrDefault("email", "unknown"))
            .forEach(email -> toReturn.add(new MetadataCheck("Distributor's email address is " + email, INFO)));
        }   

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
            .filter(publisher -> fieldNotEqual(publisher, "organisationName", "NERC Environmental Information Data Centre"))
            .map(publisher -> publisher.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Publisher name is " + organisationName, INFO)));

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
            .filter(custodian -> fieldNotEqual(custodian, "organisationName", "Environmental Information Data Centre"))
            .map(custodian -> custodian.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Custodian name is " + organisationName, INFO)));

        custodians.stream()
            .filter(custodian -> fieldNotEqual(custodian, "email", "eidc@ceh.ac.uk"))
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
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }


    private boolean fieldNotEqual(Map<String, String> map, String key, String value) {
        return !map.containsKey(key)
            || map.get(key) == null
            || !map.get(key).equals(value);
    }

    Optional<MetadataCheck> checkDataFormat(DocumentContext parsed) {
        val dataFormats = parsed.read(
            "$.distributionFormats[*]",
            typeRefStringString
        );
        if (dataFormats.isEmpty()) {
            return Optional.of(new MetadataCheck("Data format is missing", INFO));
        }
        if (dataFormats.stream().anyMatch(format -> fieldIsMissing(format, "version"))) {
            return Optional.of(new MetadataCheck("Format version is empty", ERROR));
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
            .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk") && !email.equals("eidc@ceh.ac.uk"))
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
            .anyMatch(order -> fieldNotStartingWith(order, "url", "https://catalogue.ceh.ac.uk/download"))) {
            toReturn.add(new MetadataCheck("Orders do not have a valid EIDC url", INFO));
        }
        if(downloads.stream()
            .anyMatch(order -> fieldNotStartingWith(order, "url", "https://catalogue.ceh.ac.uk/datastore/eidchub/"))) {
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

    public enum Severity {
        ERROR(1), WARNING(2), INFO(3);

        private final int priority;

        Severity(int priority) {
            this.priority = priority;
        }
    }

    @Value
    public static class MetadataCheck {
        private final String test;
        private final Severity severity;
    }

    @Value
    public static class Results {
        private final List<MetadataCheck> problems;
        private final String id, message;

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
    public static class CatalogueResults {
        private final List<Results> results;

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

        public long getTotalInfo() {
            return results.stream()
                .mapToLong(Results::getInfo)
                .sum();
        }
    }
}
