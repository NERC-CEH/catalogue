package uk.ac.ceh.gateway.catalogue.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.GitDocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.LinkDatabase;
import uk.ac.ceh.gateway.catalogue.linking.RdbmsLinkDatabase;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

@Configuration
public class LinkingConfig {
    private final DataRepository<CatalogueUser> dataRepository;
    private final BundledReaderService<GeminiDocument> bundledReaderService;
    private final LinkDatabase linkDatabase;

    @Autowired
    public LinkingConfig(DataRepository<CatalogueUser> dataRepository,
                         BundledReaderService<GeminiDocument> bundledReaderService,
                         DataSource dataSource) {
        this.dataRepository = dataRepository;
        this.bundledReaderService = bundledReaderService;
        linkDatabase = new RdbmsLinkDatabase(dataSource);
    }
    
    @Bean
    public DocumentLinkService linkingService() {
        return new GitDocumentLinkService(dataRepository, bundledReaderService, linkDatabase);
    }

}