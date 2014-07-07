package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.linking.DatabaseDocumentLinkingService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingService;
import uk.ac.ceh.gateway.catalogue.linking.LinkingRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

@Configuration
public class LinkingConfig {
    private final DataRepository<CatalogueUser> dataRepository;
    private final BundledReaderService<GeminiDocument> bundledReaderService;
    private final LinkingRepository linkingRepository;

    @Autowired
    public LinkingConfig(DataRepository<CatalogueUser> dataRepository,
                         BundledReaderService<GeminiDocument> bundledReaderService) {
        this.dataRepository = dataRepository;
        this.bundledReaderService = bundledReaderService;
        linkingRepository = new LinkingRepository() {

            @Override
            public void delete(GeminiDocument document) throws DocumentLinkingException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void add(GeminiDocument document) throws DocumentLinkingException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
    
    @Bean
    public DocumentLinkingService linkingService() {
        return new DatabaseDocumentLinkingService(dataRepository, bundledReaderService, linkingRepository);
    }

}