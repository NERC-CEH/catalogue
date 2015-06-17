package uk.ac.ceh.gateway.catalogue.services;

import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.OfflineTerraCatalogUserFactory;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.TerraCatalogDocumentInfoFactory;
import uk.ac.ceh.gateway.catalogue.util.terracatalog.TerraCatalogImporter;

/**
 *
 * @author cjohn
 */
public class TerraCatalogImporterServiceTest {
    @Mock DataRepository<CatalogueUser> repo;
    @Mock DocumentListingService listingService;
    @Mock OfflineTerraCatalogUserFactory<CatalogueUser> userFactory;
    @Mock DocumentReadingService documentReader;
    @Mock DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock TerraCatalogDocumentInfoFactory<MetadataInfo> infoFactory;
    
    TerraCatalogImporterService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        service = new TerraCatalogImporterService(repo, listingService, userFactory, documentReader, documentInfoMapper, infoFactory);
    }
    
    @Test
    public void checkThatCanCreateTerraCatalogImporter() {
        //Given        
        CatalogueUser user = mock(CatalogueUser.class);
        
        //When
        TerraCatalogImporter<MetadataInfo, CatalogueUser> importer = service.getImporter(user);
        
        //Then
        assertNotNull("Expected an importer for the given user", importer);
    }
}
