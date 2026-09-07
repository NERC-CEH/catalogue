package uk.ac.ceh.gateway.catalogue.exports;

import org.springframework.context.annotation.Profile;

import java.util.Date;

@Profile("exports")
public interface CatalogueExportService {
    void runExport();

    /**
     * When the export last completed successfully, or {@code null} if it has never completed.
     * Implementations that don't track this can leave the default.
     */
    default Date getLastExported() {
        return null;
    }
}
