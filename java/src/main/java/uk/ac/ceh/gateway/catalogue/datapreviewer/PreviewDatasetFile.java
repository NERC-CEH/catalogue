package uk.ac.ceh.gateway.catalogue.datapreviewer;

record PreviewDatasetFile(
    String name,
    String path,
    long size,
    String mimeType
) {}
