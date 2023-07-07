package uk.ac.ceh.gateway.catalogue.imports;

import org.springframework.context.annotation.Profile;

@Profile("imports")
public interface CatalogueImportService {
    void runImport();
}
