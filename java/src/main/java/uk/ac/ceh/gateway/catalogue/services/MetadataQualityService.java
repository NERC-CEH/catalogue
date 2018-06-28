package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@SuppressWarnings("unused")
@Service
@AllArgsConstructor
public class MetadataQualityService {
    private final DocumentRepository repository;
    private final Set<String> validResourceTypes = Sets.newHashSet(
        "dataset",
        "nonGeographicDataset",
        "application",
        "signpost",
        "service"
    );

    @SneakyThrows
    public Results check(String id) {
        val document = this.repository.read(id);
        List<MetadataCheck> toReturn = new ArrayList<>();

        if (document instanceof GeminiDocument) {
            val gemini = (GeminiDocument)document;
            val resourceType = Optional.ofNullable(gemini.getResourceType()).map(Keyword::getValue).orElse("unknown");
            val metadata = Optional.ofNullable(gemini.getMetadata()).orElse(MetadataInfo.builder().build());

            if (validResourceTypes.contains(resourceType)) {
                toReturn.add(isMissing("Resource type", resourceType, 1));
                toReturn.add(isMissing("Title", gemini.getTitle(), 1));
                toReturn.add(isMissing("Description", gemini.getDescription(), 1));
                toReturn.add(isMissing("Lineage", gemini.getLineage(), 1));
                toReturn.add(isMissing("Resource status", gemini.getResourceStatus(), 1));
                if ("draft".equals(metadata.getState())) {
                    toReturn.add(isMissing("Publication date", gemini.getResourceStatus(), 1));
                }
                toReturn.add(hasLicences(gemini));
                toReturn.add(hasCount("Licence", getLicences(gemini), 1, 1));
                toReturn.add(emptyTemporalExtents(gemini));
            }
        }
        return new Results(toReturn);
    }

    MetadataCheck emptyTemporalExtents(GeminiDocument gemini) {
        val temporalExtents = Optional.ofNullable(gemini.getTemporalExtents());
        if (temporalExtents.isPresent()) {
            String result;
            if (Optional.ofNullable(gemini.getTemporalExtents())
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(tp -> tp.getBegin() == null && tp.getEnd() == null)) {
                result = "fail";
            } else {
                result = "pass";
            }
            return new MetadataCheck("Temporal extents is empty", result, 1);
        } else {
            return new MetadataCheck("Temporal extents is missing", "fail", 1);
        }
    }

    MetadataCheck isMissing(String element, String target, int severity) {
        String result;

        if (target == null || target.isEmpty()) {
            result = "fail";
        } else {
            result = "pass";
        }
        return new MetadataCheck(format("%s is missing", element), result, severity);
    }

    private List<ResourceConstraint> getLicences(GeminiDocument gemini) {
        return Optional.ofNullable(gemini.getUseConstraints())
            .orElse(Collections.emptyList())
            .stream()
            .filter(rc -> rc.getCode().equals("license"))
            .collect(Collectors.toList());
    }

    MetadataCheck hasLicences(GeminiDocument gemini) {
        String result;
        if (Optional.ofNullable(gemini.getUseConstraints())
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(rc -> rc.getCode().equals("license")))
        {
            result = "pass";
        } else {
            result = "fail";
        }
        return new MetadataCheck("Licence is missing", result, 1);
    }

    MetadataCheck hasCount(String element, List target, int severity, int requirement) {
        String result;
        if (Optional.ofNullable(target)
            .orElse(Collections.emptyList())
            .size() == requirement)
        {
            result = "pass";
        } else {
            result = "fail";
        }
        return new MetadataCheck(format("%s count should be %s", element, requirement), result, severity);
    }

    @Value
    public static class MetadataCheck {
        private final String test, result;
        private final int severity;
    }

    @Value
    public static class Results {
        private final List<MetadataCheck> problems;

        public long getErrors() {
            return problems.stream()
                .filter(m -> m.getSeverity() == 1)
                .count();
        }

        public long getWarnings() {
            return problems.stream()
                .filter(m -> m.getSeverity() == 3)
                .count();
        }
    }
}
