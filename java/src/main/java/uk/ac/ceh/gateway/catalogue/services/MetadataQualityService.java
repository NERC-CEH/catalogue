package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.ac.ceh.gateway.catalogue.services.MetadataQualityService.Failure.ERROR;
import static uk.ac.ceh.gateway.catalogue.services.MetadataQualityService.Failure.WARNING;

@Slf4j
@SuppressWarnings("unused")
@AllArgsConstructor
public class MetadataQualityService {
    private final DocumentReader documentReader;
    private final Set<String> validResourceTypes = Sets.newHashSet(
        "dataset",
        "nonGeographicDataset",
        "application",
        "signpost",
        "service"
    );

    @SneakyThrows
    public Results check(String id) {
        log.info("Checking {}", id);


        val config = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        val parsedDoc = JsonPath.parse(documentReader.read(id, "raw"), config);
        val parsedMeta = JsonPath.parse(documentReader.read(id, "meta"), config);


        List<MetadataCheck> toReturn = Arrays.asList(
            isMissing(parsedDoc,"Resource type", "resourceType"),
            isMissing(parsedDoc,"Title", "title"),
            isMissing(parsedDoc,"Description", "description"),
            isMissing(parsedDoc,"Lineage", "lineage"),
            isMissing(parsedDoc,"Resource status", "resourceStatus")
        );


//        if (document instanceof GeminiDocument) {
//            val gemini = (GeminiDocument)document;
//            val resourceType = Optional.ofNullable(gemini.getResourceType()).map(Keyword::getValue).orElse("unknown");
//            val metadata = Optional.ofNullable(gemini.getMetadata()).orElse(MetadataInfo.builder().build());
//
//            if (validResourceTypes.contains(resourceType)) {
//                toReturn.add(isMissing("Resource type", resourceType, 1));
//                toReturn.add(isMissing("Title", gemini.getTitle(), 1));
//                toReturn.add(isMissing("Description", gemini.getDescription(), 1));
//                toReturn.add(isMissing("Lineage", gemini.getLineage(), 1));
//                toReturn.add(isMissing("Resource status", gemini.getResourceStatus(), 1));
//                if ("draft".equals(metadata.getState())) {
//                    toReturn.add(isMissing("Publication date", gemini.getResourceStatus(), 1));
//                }
//                toReturn.add(hasLicences(gemini));
//                toReturn.add(hasCount("Licence", getLicences(gemini), 1, 1));
//                toReturn.add(emptyTemporalExtents(gemini));
//            }
//        }
        return new Results(toReturn);
    }

    MetadataCheck emptyTemporalExtents(GeminiDocument gemini) {
        val temporalExtents = ofNullable(gemini.getTemporalExtents());
        if (temporalExtents.isPresent()) {
            String result;
            if (ofNullable(gemini.getTemporalExtents())
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(tp -> tp.getBegin() == null && tp.getEnd() == null)) {
                result = "fail";
            } else {
                result = "pass";
            }
            return new MetadataCheck("Temporal extents is empty", result, ERROR);
        } else {
            return new MetadataCheck("Temporal extents is missing", "fail", ERROR);
        }
    }

    MetadataCheck isMissing(DocumentContext parsed, String element, String path) {
        val message = format("%s is missing", element);
        val testPath = format("$[?(@.%s empty false)]", path);
        log.debug("path: {} produces: {}", testPath, parsed.read(testPath));

        if (((List)parsed.read(testPath)).isEmpty())
        {
            return new MetadataCheck(message, "fail", ERROR);
        } else {
            return new MetadataCheck(message, "pass", ERROR);
        }
    }

    private List<ResourceConstraint> getLicences(GeminiDocument gemini) {
        return ofNullable(gemini.getUseConstraints())
            .orElse(Collections.emptyList())
            .stream()
            .filter(rc -> rc.getCode().equals("license"))
            .collect(Collectors.toList());
    }

    MetadataCheck hasLicences(GeminiDocument gemini) {
        String result;
        if (ofNullable(gemini.getUseConstraints())
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(rc -> rc.getCode().equals("license")))
        {
            result = "pass";
        } else {
            result = "fail";
        }
        return new MetadataCheck("Licence is missing", result, ERROR);
    }

    MetadataCheck hasCount(String element, List target, Failure failure, int requirement) {
        String result;
        if (ofNullable(target)
            .orElse(Collections.emptyList())
            .size() == requirement)
        {
            result = "pass";
        } else {
            result = "fail";
        }
        return new MetadataCheck(format("%s count should be %s", element, requirement), result, failure);
    }

    public enum Failure {
        ERROR, WARNING
    }

    @Value
    public static class MetadataCheck {
        private final String test, result;
        private final Failure failure;
    }

    @Value
    public static class Results {
        private final List<MetadataCheck> problems;

        public long getErrors() {
            return problems.stream()
                .filter(m -> ERROR.equals(m.getFailure()))
                .count();
        }

        public long getWarnings() {
            return problems.stream()
                .filter(m -> WARNING.equals(m.getFailure()))
                .count();
        }
    }
}
