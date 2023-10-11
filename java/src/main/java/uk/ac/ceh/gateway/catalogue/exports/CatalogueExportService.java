package uk.ac.ceh.gateway.catalogue.exports;

import org.springframework.context.annotation.Profile;

@Profile("exports")
public interface CatalogueExportService {
    void runExport();
}
