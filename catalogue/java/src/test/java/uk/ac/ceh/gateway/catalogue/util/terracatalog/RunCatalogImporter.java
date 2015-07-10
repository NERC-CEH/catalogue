package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.gateway.catalogue.config.ApplicationConfig;
import uk.ac.ceh.gateway.catalogue.config.CrowdUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.converters.Xml2GeminiDocumentMessageConverter;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.ExtensionDocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.JacksonDocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.MessageConverterReadingService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class, CrowdUserStoreConfig.class})
public class RunCatalogImporter {
    @Autowired ObjectMapper jacksonMapper;
    @Autowired private DataRepository<CatalogueUser> repo;
    @Autowired AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory;
    @Autowired CodeLookupService codeLookupService;
    
    @BeforeClass
    public static void before() {
        System.setProperty("config.file", "E:/repos/cig/vagrant/catalogue_config/development.properties");
    }
    
    @Test
    @Ignore("This is not really a test, it is a way of creating a Git repository from terraCatalog export Zips")
    // N.B. change data.repository.location in developer.properties to a temp location before running.
    public void toImport() throws Exception {
        DocumentReadingService documentReader = new MessageConverterReadingService()
                .addMessageConverter(new Xml2GeminiDocumentMessageConverter(codeLookupService));
        DocumentInfoMapper<MetadataInfo> documentInfoMapper = new JacksonDocumentInfoMapper(jacksonMapper, MetadataInfo.class);
        DocumentListingService documentList = new ExtensionDocumentListingService();
        DocumentIdentifierService documentIdentifierService = new DocumentIdentifierService("https://catalogue.ceh.ac.uk", '-');
        OfflineTerraCatalogUserFactory<CatalogueUser> userFactory = new OfflineTerraCatalogUserFactory<>(phantomUserBuilderFactory);
        userFactory.put("ceh", "@ceh.ac.uk");
        StateTranslatingMetadataInfoFactory infoFactory = new StateTranslatingMetadataInfoFactory();
        infoFactory.put("private", "draft");
        infoFactory.put("public", "published");
        CatalogueUser importUser = new CatalogueUser();
        importUser.setUsername("Import");
        importUser.setEmail("import@example.com");
        TerraCatalogImporter<MetadataInfo, CatalogueUser> importer = new TerraCatalogImporter<>(
            repo,
            documentList,
            documentIdentifierService,
            userFactory,
            documentReader,
            documentInfoMapper,
            infoFactory,
            importUser);
        importer.importDirectory(new File("C:/users/rjsc/desktop/backup"));
    }
}