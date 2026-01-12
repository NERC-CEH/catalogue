package uk.ac.ceh.gateway.catalogue.datapreviewer;

import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;

import java.util.List;
import java.util.Map;

public record DatasetPreviewResponse(
    String type,
    String id,
    String title,
    List<TimePeriod> timePeriods,
    Map<String, String> observedProperties,
    List<PreviewDatasetFile> files
) {}
