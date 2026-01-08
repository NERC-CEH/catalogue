package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.List;
import java.util.Map;

public record DatasetPreviewResponse(
    String type,
    String id,
    String title,
    Map<String, String> observedProperties,
    List<PreviewDatasetFile> files
) {}
