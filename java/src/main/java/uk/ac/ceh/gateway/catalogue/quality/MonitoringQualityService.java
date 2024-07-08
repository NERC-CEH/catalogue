package uk.ac.ceh.gateway.catalogue.quality;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream
;
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

    private final TypeRef<Map<String, String>> typeRefStringString = new TypeRef<>() {};

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

            val checks = Stream.of(
                checkKeywords(parsedDoc),
                checkOperatingPeriod(parsedMeta, parsedDoc)
            ).flatMap(s -> s).collect(Collectors.toCollection(ArrayList::new));

            checks.addAll(switch (parsedMeta.read("$.documentType", String.class)) {
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

    private Stream<MetadataCheck> checkKeywords(DocumentContext parsedDoc) {
        val keywords = parsedDoc.read("$.keywords", List.class);
        return checkNonEmptyIfPresent("Keywords", keywords);
    }

    private Stream<MetadataCheck> checkOperatingPeriod(DocumentContext parsedMeta, DocumentContext parsedDoc) {
        if (parsedMeta.read("$.documentType", String.class).equals(MONITORING_NETWORK)) {
            return Stream.empty();
        }

        val periods = parsedDoc.read("$.operatingPeriod", List.class);
        return checkNonEmptyIfPresent("Operating period", periods);
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

        return toReturn.stream();
    }

    private Stream<MetadataCheck> checkProgrammeOrActivity(DocumentContext parsedDoc) {
        return checkBoundingBox(parsedDoc);
    }

    private Stream<MetadataCheck> checkNonEmptyIfPresent(String field, List<?> list) {
        return Optional.ofNullable(list)
            .flatMap(l -> {
                if (l.isEmpty()) {
                    return Optional.of(new MetadataCheck(field + " is empty", ERROR));
                } else {
                    return Optional.empty();
                }
            })
            .stream();
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
}
