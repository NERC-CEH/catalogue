package uk.ac.ceh.gateway.catalogue.util.terracatalog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.gateway.catalogue.config.ApplicationConfig;
import uk.ac.ceh.gateway.catalogue.config.CrowdUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.services.ExtensionDocumentListingService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfig.class, CrowdUserStoreConfig.class,})
public class RunCatalogImporter {
    @Autowired private DataRepository<CatalogueUser> repo;
    private final TerraCatalogUserFactory<CatalogueUser> userFactory;
    @Autowired private DocumentReadingService<GeminiDocument> documentReader;
    @Autowired private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Autowired AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory;
    private final TerraCatalogImporter<MetadataInfo, CatalogueUser> importer;

    public RunCatalogImporter() {
        DocumentListingService documentList = new ExtensionDocumentListingService();
        Map<String, String> groupToDomain = new  HashMap<>();
        groupToDomain.put("ceh", "@ceh.ac.uk");
        userFactory = new OfflineTerraCatalogUserFactory<>(groupToDomain, phantomUserBuilderFactory);
        Map<String, String> statusToState = new HashMap<>();
        statusToState.put("private", "draft");
        statusToState.put("public", "public");
        TerraCatalogDocumentInfoFactory<MetadataInfo> infoFactory = new StateTranslatingMetadataInfoFactory(statusToState);
        CatalogueUser importUser = new CatalogueUser();
        importUser.setUsername("Import");
        importUser.setEmail("import@example.com");
        importer = new TerraCatalogImporter<>(
            repo,
            documentList,
            userFactory,
            documentReader,
            documentInfoMapper,
            infoFactory,
            importUser);
    }
    
    @Test
    public void toImport() throws Exception {
        importer.importDirectory(new File("c:\temp\tcimport"));
    }
}