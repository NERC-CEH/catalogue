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

@Slf4j
@SuppressWarnings({"unused", "WeakerAccess"})
@Service
public class MetadataQualityService {
    private final DocumentReader documentReader;
    private final Configuration config;
    private final Set<String> validResourceTypes = ImmutableSet.of(
        "aggregate",
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
            checkNonAggregates(parsedDoc).ifPresent(checks::addAll);
            checkAddress(parsedDoc, "Metadata point of contact", "metadataPointsOfContact", ERROR).ifPresent(checks::addAll);
            checkPointsOfContact(parsedDoc).ifPresent(checks::addAll);
            checkNonGeographicDatasets(parsedDoc).ifPresent(checks::addAll);
            checkSignpost(parsedDoc).ifPresent(checks::add);
            checkDataset(parsedDoc).ifPresent(checks::addAll);
            checkStuff(parsedDoc).ifPresent(checks::addAll);
            checkDownloadAndOrderLinks(parsedDoc).ifPresent(checks::addAll);
            return new Results(checks);
        } else {
            return new Results(Collections.emptyList());
        }
    }

    private boolean isQualifyingDocument(DocumentContext parsedDoc, DocumentContext parsedMeta) {
        return isEqual(parsedMeta, "documentType", CatalogueServiceConfig.GEMINI_DOCUMENT) && isCorrectResourceType(parsedDoc);
    }

    private Optional<List<MetadataCheck>> checkBasics(DocumentContext parsedDoc) {
        val toReturn = new ArrayList<MetadataCheck>();
        isMissing(parsedDoc, "Resource type", "resourceType").ifPresent(toReturn::add);
        isMissing(parsedDoc, "Title", "title").ifPresent(toReturn::add);
        isMissing(parsedDoc, "Description", "description").ifPresent(toReturn::add);
        isMissing(parsedDoc, "Lineage", "lineage").ifPresent(toReturn::add);
        isMissing(parsedDoc, "Resource status", "resourceStatus").ifPresent(toReturn::add);
        return Optional.of(toReturn);
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

    private Optional<List<MetadataCheck>> checkNonAggregates(DocumentContext parsedDoc) {
        if ( !notRequiredResourceTypes(parsedDoc, "aggregate")) {
            return Optional.empty();
        }
        val toReturn = new ArrayList<MetadataCheck>();
        val licences = parsedDoc.read("$.useConstraints[*][?(@.code == 'license')]", typeRefStringString);
        if (licences.isEmpty()) {
            toReturn.add(new MetadataCheck("Licence is missing", ERROR));
        }
        if (licences.size() != 1) {
            toReturn.add(new MetadataCheck("Licences count should be 1", ERROR));
        }
        val temporalExtents = parsedDoc.read("$.temporalExtents[*]", typeRefStringString);
        if (temporalExtents.isEmpty()) {
            toReturn.add(new MetadataCheck("Temporal extents is missing", ERROR));
        }
        if (temporalExtents.stream().anyMatch(this::beginAndEndBothEmpty)) {
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

    private boolean isEqual(DocumentContext parsed, String path, String equal) {
        return (Optional.ofNullable(parsed.read(path))
            .orElse("")
            .equals(equal)
        );
    }

    private boolean isCorrectResourceType(DocumentContext parsed) {
        return validResourceTypes.contains(parsed.read("$.resourceType.value", String.class));
    }

    boolean isEmpty(DocumentContext parsed, String path) {
        return Optional.ofNullable(parsed.read(path, List.class))
            .orElse(Collections.emptyList())
            .isEmpty();
    }

    Optional<MetadataCheck> isMissing(DocumentContext parsed, String element, String path) {
        return isMissingPath(parsed,
            format("%s is missing", element),
            format("$[?(@.%s empty false)]", path)
        );
    }

    Optional<MetadataCheck> isMissingPath(DocumentContext parsed, String message, String path) {
        if (isEmpty(parsed, path)) {
            return Optional.of(new MetadataCheck(message, ERROR));
        } else {
            return Optional.empty();
        }
    }

    Optional<MetadataCheck> hasCount(DocumentContext parsed, String element, String path, int requirement) {
        val message = format("%s count should be %s", element, requirement);
        if (Optional.ofNullable(parsed.read(path, List.class))
            .orElse(Collections.emptyList())
            .size() == requirement
            )
        {
            return Optional.empty();
        } else {
            return Optional.of(new MetadataCheck(message, ERROR));
        }
    }

    Optional<List<MetadataCheck>> checkAddress(DocumentContext parsed, String element, String path, Severity severity) {
        return checkAddress(
            parsed.read(
                format("$.%s[*].['organisationName','email']", path),
                typeRefStringString
            ),
            element
        );
    }

    Optional<List<MetadataCheck>> checkAddress(List<Map<String, String>> addresses, String element) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (addresses == null || addresses.isEmpty()) {
            return Optional.of(Collections.singletonList(new MetadataCheck(element + " is missing", ERROR)));
        }
        if (addresses.stream().anyMatch(this::isMissingOrganisationName)) {
            toReturn.add(new MetadataCheck(element + " organisation name is missing", ERROR));
        }
        reportNonStandardEmail(addresses, element).ifPresent(toReturn::addAll);
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    private boolean isMissingOrganisationName(Map<String, String> address) {
        return !address.containsKey("organisationName") || (address.get("organisationName") == null || address.get("organisationName").isEmpty());
    }

    Optional<MetadataCheck> checkOrganisationName(DocumentContext parsed, String element, String path, Severity severity) {
        val testPath = format("$.%s[*][?(@.organisationName empty true || @.organisationName == null)]", path);
        val message = format("%s organisation name is missing", element);
        if (parsed.read(testPath, List.class).isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new MetadataCheck(message, severity));
        }
    }

    Optional<List<MetadataCheck>> reportNonStandardEmail(DocumentContext parsed, String element, String path, Severity severity) {
        val addresses = parsed.read(format("$.%s[*]", path), typeRefStringString);
        return reportNonStandardEmail(addresses, element);
    }

    Optional<List<MetadataCheck>> reportNonStandardEmail(List<Map<String, String>> addresses, String element) {
        return Optional.of(addresses.stream()
            .filter(address -> address.containsKey("email"))
            .map(address -> address.get("email"))
            .filter(email -> email.endsWith("@ceh.ac.uk") && !email.equals("enquiries@ceh.ac.uk") && !email.equals("eidc@ceh.ac.uk"))
            .map(email -> new MetadataCheck(format("%s email address is %s", element, email), WARNING))
            .collect(Collectors.toList()));
    }

    Optional<List<MetadataCheck>> checkPointsOfContact(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        hasCount(parsed, "Metadata point of contact", "metadataPointsOfContact", 1).ifPresent(toReturn::add);
        if (parsed.read("$.metadataPointsOfContact[*][?(@.role == 'pointOfContact')]", List.class).isEmpty()) {
            toReturn.add(new MetadataCheck("Metadata point of contact MUST have the role 'Point of Contact'", ERROR));
        }
        isMissingPath(parsed, "Metadata point of contact email is missing","metadataPointsOfContact[*][?(@.email empty false)]").ifPresent(toReturn::add);
        isMissingPath(parsed, "Metadata point of contact name is missing","metadataPointsOfContact[*][?(@.individualName empty false)]").ifPresent(toReturn::add);
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
        if (parsed.read("$[?(@.boundingBoxes empty true)].id", List.class).isEmpty()) {
            toReturn.add(new MetadataCheck("The record has a bounding box but the resource type is Non-geographic dataset", WARNING));
        }
        if (parsed.read("$[?(@.spatialRepresentationTypes empty true)].id", List.class).isEmpty()) {
            toReturn.add(new MetadataCheck("The record has a spatial representation type but the resource type is Non-geographic dataset", WARNING));
        }
        if (parsed.read("$[?(@.spatialReferenceSystems empty true)].id", List.class).isEmpty()) {
            toReturn.add(new MetadataCheck("The record has a spatial reference system but the resource type is Non-geographic dataset", WARNING));
        }
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
            return Optional.of(new MetadataCheck("There are more than one search/information links", ERROR));
        }
    }

    Optional<List<MetadataCheck>> checkDataset(DocumentContext parsed) {
        if (notRequiredResourceTypes(parsed, "dataset")) {
            return Optional.empty();
        }
        val toReturn = new ArrayList<MetadataCheck>();
        checkInspireTheme(parsed).ifPresent(toReturn::addAll);
        checkBoundingBoxes(parsed).ifPresent(toReturn::addAll);
        isMissing(parsed, "Spatial extent", "boundingBoxes").ifPresent(toReturn::add);
        isMissing(parsed, "Spatial representation type", "spatialRepresentationTypes").ifPresent(toReturn::add);
        isMissing(parsed, "Spatial reference system", "spatialReferenceSystems").ifPresent(toReturn::add);
        if (toReturn.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toReturn);
        }
    }

    Optional<List<MetadataCheck>> checkInspireTheme(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        if (parsed.read("$.descriptiveKeywords[*][?(@.type == 'INSPIRE Theme')].type", List.class).isEmpty()) {
            toReturn.add(new MetadataCheck("INSPIRE theme is missing", ERROR));
            return Optional.of(toReturn);
        }
        if (parsed.read("$.descriptiveKeywords[*][?(@.type == 'INSPIRE Theme')].keywords[*]", List.class).size() == 0) {
            toReturn.add(new MetadataCheck("INSPIRE theme is empty", ERROR));
            return Optional.of(toReturn);
        }
        if (parsed.read(
            "$.descriptiveKeywords[*][?(@.type == 'INSPIRE Theme')].keywords[*][?(@.uri contains 'http://inspire.ec.europa.eu/theme/')].uri",
            List.class
        ).isEmpty()) {
            toReturn.add(new MetadataCheck("INSPIRE theme does not have correct URI", ERROR));
        }
        if (parsed.read(
            "$.descriptiveKeywords[*][?(@.type == 'INSPIRE Theme')].keywords[*][?(@.uri empty false)]",
            List.class
        ).isEmpty()) {
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
        checkAddress(distributors, "Distributor").ifPresent(toReturn::addAll);
        if (distributors.size() != 1) {
            toReturn.add(new MetadataCheck("Distributor count should be 1", ERROR));
        }
        if (nonDistributors.size() > 0) {
            toReturn.add(new MetadataCheck("Distributor contact must have role 'distributor'", ERROR));
        }
        if (distributors.stream().anyMatch(custodian -> fieldIsMissing(custodian, "email"))) {
            toReturn.add(new MetadataCheck("Distributor's email address is missing", ERROR));
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
        if (publishers.size() != 1) {
            toReturn.add(new MetadataCheck("Publisher count should be 1", ERROR));
        }
        if (publishers.stream().anyMatch(custodian -> fieldIsMissing(custodian, "email"))) {
            toReturn.add(new MetadataCheck("Publisher email address is missing", ERROR));
        }
        publishers.stream()
            .filter(custodian -> fieldNotEqual(custodian, "organisationName", "NERC Environmental Information Data Centre"))
            .map(custodian -> custodian.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Publisher name is " + organisationName, WARNING)));

        publishers.stream()
            .filter(custodian -> fieldNotEqual(custodian, "email", "eidc@ceh.ac.uk"))
            .map(custodian -> custodian.getOrDefault("email", "unknown"))
            .forEach(email -> toReturn.add(new MetadataCheck("Publisher email address is " + email, WARNING)));

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
        if (custodians.size() != 1) {
            toReturn.add(new MetadataCheck("Custodian count should be 1", ERROR));
        }
        if (custodians.stream().anyMatch(custodian -> fieldIsMissing(custodian, "email"))) {
            toReturn.add(new MetadataCheck("Custodian email address is missing", ERROR));
        }
        custodians.stream()
            .filter(custodian -> fieldNotEqual(custodian, "organisationName", "Environmental Information Data Centre"))
            .map(custodian -> custodian.getOrDefault("organisationName", "unknown"))
            .forEach(organisationName -> toReturn.add(new MetadataCheck("Custodian name is " + organisationName, WARNING)));

        custodians.stream()
            .filter(custodian -> fieldNotEqual(custodian, "email", "eidc@ceh.ac.uk"))
            .map(custodian -> custodian.getOrDefault("email", "unknown"))
            .forEach(email -> toReturn.add(new MetadataCheck("Custodian email address is " + email, WARNING)));

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
            return Optional.of(new MetadataCheck("Data format is missing", ERROR));
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

    Optional<List<MetadataCheck>> checkAuthors(DocumentContext parsed) {
        val toReturn = new ArrayList<MetadataCheck>();
        val authors = parsed.read(
            "$.responsibleParties[*][?(@.role == 'author')].['individualName', 'organisationName','email']",
            typeRefStringString
            );
        checkAddress(authors, "Authors").ifPresent(toReturn::addAll);
        if (authors.stream().anyMatch(author -> fieldIsMissing(author, "individualName"))) {
            toReturn.add(new MetadataCheck("Author's name is missing", ERROR));
        }
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

    Optional<List<MetadataCheck>> checkDownloadAndOrderLinks(DocumentContext parsed) {
        if (resourceStatusIsNotCurrent(parsed) && notRequiredResourceTypes(parsed, "dataset", "nonGeographicDataset", "application")
        ) {
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
            toReturn.add(new MetadataCheck("There are no orders/downloads", ERROR));
        } else if (totalOrdersAndDownloads > 1) {
            toReturn.add(new MetadataCheck("There are multiple orders/downloads", WARNING));
        }
        if(orders.stream()
            .anyMatch(order -> fieldNotStartingWith(order, "url", "https://catalogue.ceh.ac.uk/download"))) {
            toReturn.add(new MetadataCheck("Orders do not have a valid EIDC url", WARNING));
        }
        if(downloads.stream()
            .anyMatch(order -> fieldNotStartingWith(order, "url", "https://catalogue.ceh.ac.uk/datastore/eidchub/"))) {
            toReturn.add(new MetadataCheck("Downloads do not have a valid EIDC url", WARNING));
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

    private boolean resourceStatusIsNotCurrent(DocumentContext parsed) {
        val resourceStatus = parsed.read("$.resourceStatus", String.class);
        return resourceStatus == null || !resourceStatus.equals("Current");
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
        ERROR(1), WARNING(3);

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

        public Results(List<MetadataCheck> problems) {
            this.problems = problems.stream()
                .sorted(Comparator.comparingInt(o -> o.getSeverity().priority))
                .collect(Collectors.toList());
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
    }
}
