package uk.ac.ceh.gateway.catalogue.config;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.linking.GitDocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkService;
import uk.ac.ceh.gateway.catalogue.linking.LinkDatabase;
import uk.ac.ceh.gateway.catalogue.linking.Metadata;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

@Configuration
public class LinkingConfig {
    private final DataRepository<CatalogueUser> dataRepository;
    private final BundledReaderService<GeminiDocument> bundledReaderService;
    private final LinkDatabase linkingRepository;

    @Autowired
    public LinkingConfig(DataRepository<CatalogueUser> dataRepository,
                         BundledReaderService<GeminiDocument> bundledReaderService) {
        this.dataRepository = dataRepository;
        this.bundledReaderService = bundledReaderService;
        linkingRepository = new LinkDatabase() {

            @Override
            public void empty() throws DocumentLinkingException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void delete(Metadata metadata) throws DocumentLinkingException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void add(Metadata metadata) throws DocumentLinkingException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void add(Collection<Metadata> metadata) throws DocumentLinkingException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Collection<Metadata> findDatasetsForService(String fileIdentifier) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Collection<Metadata> findServicesForDataset(String fileIdentifier) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
    
    @Bean
    public DocumentLinkService linkingService() {
        return new GitDocumentLinkService(dataRepository, bundledReaderService, linkingRepository);
    }

}