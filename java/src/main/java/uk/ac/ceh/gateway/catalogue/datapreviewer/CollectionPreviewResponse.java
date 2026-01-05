package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.List;
import java.util.Map;

public record CollectionPreviewResponse(
    String type,
    String id,
    String title,
    Map<String, String> observedProperties,
    List<DatasetEntry> datasets
) {
    public record DatasetEntry(
        String id,
        String title,
        List<PreviewDatasetFile> files
    ) {}
}
