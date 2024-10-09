package uk.ac.ceh.gateway.catalogue.quality;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static uk.ac.ceh.gateway.catalogue.DocumentTypes.*;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.*;

@Service
@Slf4j
@Qualifier("monitoring")
public class MonitoringQualityService implements MetadataQualityService {
    private final DocumentReader documentReader;
    private final Configuration config;

    private static final TypeRef<Map<String, String>> typeRefStringString = new TypeRef<>() {};
    private static final TypeRef<List<Map<String, String>>> typeRefListStringString = new TypeRef<>() {};
    private static final DateFormat operatingPeriodFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MonitoringQualityService(
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

    @Override
    public Results check(String id) {
        log.debug("Checking {}", id);

        try {
            val parsedDoc = JsonPath.parse(documentReader.read(id, "raw"), config);
            val parsedMeta = JsonPath.parse(documentReader.read(id, "meta"), config);

            val checks = checkKeywords(parsedDoc).collect(Collectors.toCollection(ArrayList::new));

            val docType = parsedMeta.read("$.documentType", String.class);

            checks.addAll(switch (docType) {
                case MONITORING_FACILITY -> checkFacility(parsedDoc).toList();
                case MONITORING_PROGRAMME, MONITORING_ACTIVITY -> checkProgrammeOrActivity(parsedDoc).toList();
                default -> Collections.emptyList();
            });

            return new Results(checks, id);
        } catch (Exception ex) {
            log.error("Error - could not check " + id, ex);
            return new Results(Collections.emptyList(), id, "Error - could not check this document");
        }
    }

    private Stream<MetadataCheck> checkFacility(DocumentContext parsedDoc) {
        val toReturn = new ArrayList<MetadataCheck>();

        val facilityType = parsedDoc.read("$.facilityType", typeRefStringString);
        if (facilityType == null) {
            toReturn.add(new MetadataCheck("Facility type is missing", ERROR));
        }

        val geometry = parsedDoc.read("$.geometry", typeRefStringString);
        if (geometry == null) {
            toReturn.add(new MetadataCheck("Geometry is missing", ERROR));
        }

        toReturn.addAll(checkOperatingPeriods(parsedDoc).toList());

        return toReturn.stream();
    }

    private Stream<MetadataCheck> checkProgrammeOrActivity(DocumentContext parsedDoc) {
        return Stream.concat(
            checkBoundingBox(parsedDoc),
            checkOperatingPeriods(parsedDoc)
        );
    }

    private Stream<MetadataCheck> checkKeywords(DocumentContext parsedDoc) {
        val keywords = parsedDoc.read("$.keywords", typeRefListStringString);
        return Stream.concat(
            checkNonEmptyIfPresent("Keywords", keywords),
            checkAllNonEmpty("Keyword", keywords)
        );
    }

    private Stream<MetadataCheck> checkOperatingPeriods(DocumentContext parsedDoc) {
        val periods = parsedDoc.read("$.operatingPeriod", typeRefListStringString);
        return Stream.of(
            checkNonEmptyIfPresent("Operating period", periods),
            checkAllNonEmpty("Operating period", periods),
            Stream.ofNullable(periods)
                .flatMap(List::stream)
                .flatMap(this::checkOperatingPeriod)
                .limit(1)
        ).flatMap(s -> s);
    }

    private Stream<MetadataCheck> checkBoundingBox(DocumentContext parsedDoc) {
        val toReturn = new ArrayList<MetadataCheck>();
        val boundingBox = parsedDoc.read(
            "$.boundingBox.['northBoundLatitude','southBoundLatitude','eastBoundLongitude','westBoundLongitude']",
            new TypeRef<Map<String, Double>>() {}
        );

        if (boundingBox == null) {
            toReturn.add(new MetadataCheck("Bounding box is missing", ERROR));
            return toReturn.stream();
        }

        Stream.of(
            "northBoundLatitude",
            "southBoundLatitude",
            "eastBoundLongitude",
            "westBoundLongitude"
        ).forEach(key -> {
            if (!boundingBox.containsKey(key)) {
                toReturn.add(new MetadataCheck(key + " is missing from bounding box", ERROR));
            }
            val limit = key.endsWith("Latitude") ? 90 : 180;
            Optional.ofNullable(boundingBox.get(key)).ifPresent(val -> {
                if (Math.abs(val) > limit) {
                    toReturn.add(new MetadataCheck(key + " is out of range", ERROR));
                }
            });
        });

        Stream.of(
            List.of("northBoundLatitude", "southBoundLatitude"),
            List.of("eastBoundLongitude", "westBoundLongitude")
        ).forEach(pair -> {
            val shouldBeMore = pair.get(0);
            val shouldBeLess = pair.get(1);
            if (boundingBox.keySet().containsAll(pair)
                && boundingBox.get(shouldBeLess) > boundingBox.get(shouldBeMore)) {
                toReturn.add(new MetadataCheck(
                    String.format("%s should be less than %s", shouldBeLess, shouldBeMore),
                    ERROR
                ));
            }
        });

        return toReturn.stream();
    }

    private Stream<MetadataCheck> checkOperatingPeriod(Map<String, String> period) {
        val begin = parseDate(period.get("begin"));
        val end = parseDate(period.get("end"));
        if (begin.isEmpty() || end.isEmpty()) {
            return Stream.empty();
        }
        if (begin.get().compareTo(end.get()) > 0) {
            return Stream.of(new MetadataCheck("Begin date is after end date in operating period", ERROR));
        }
        return Stream.empty();
    }

    private <K, V> Stream<MetadataCheck> checkAllNonEmpty(String field, List<Map<K, V>> items) {
        return Stream.ofNullable(items)
            .flatMap(List::stream)
            .filter(Map::isEmpty)
            .map(_m -> new MetadataCheck(field + " is empty", ERROR))
            .limit(1);
    }

    private Stream<MetadataCheck> checkNonEmptyIfPresent(String field, List<?> list) {
        return Stream.ofNullable(list)
            .filter(List::isEmpty)
            .map(_l -> new MetadataCheck(field + " is empty", ERROR));
    }

    private Optional<Date> parseDate(String str) {
        return Optional.ofNullable(str).flatMap(s -> {
            try {
                return Optional.ofNullable(operatingPeriodFormat.parse(s));
            } catch (ParseException _e) {
                return Optional.empty();
            }
        });
    }
}
