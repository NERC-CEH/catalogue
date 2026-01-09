package uk.ac.ceh.gateway.catalogue.datapreviewer;

import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;

import java.util.List;
import java.util.Map;

public record CollectionPreviewResponse(
    String type,
    String id,
    String title,
    Map<String, String> observedProperties,
    List<DatasetEntry> datasets,
    List<CollectionEntry> collections
) {
    public record DatasetEntry(
        String id,
        String title,
        List<TimePeriod> timePeriods,
        Map<String, String> observedProperties,
        List<PreviewDatasetFile> files
    ) {}

    public record CollectionEntry(
        String id,
        String title,
        List<DatasetEntry> datasets,
        List<CollectionEntry> collections
    ) {}
}
