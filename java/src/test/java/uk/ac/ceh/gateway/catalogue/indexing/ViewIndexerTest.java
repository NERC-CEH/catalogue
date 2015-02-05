package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

public class ViewIndexerTest {
    @Mock private DataRepository<CatalogueUser> repo;
    private ViewIndexer indexer;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        indexer = new ViewIndexer(repo);
    }

    @Test
    public void fullViewAndAuthor() throws DataRepositoryException {
        //Given
        List<String> expected = Arrays.asList("bob", "sally", "fred");
        given(repo.getRevisions("test.meta")).willReturn(Arrays.asList(createDataRevision("fred")));
        MetadataInfo info = new MetadataInfo();
        info.addPermissions(Permission.VIEW, Arrays.asList("bob", "sally"));
        GeminiDocument document = new GeminiDocument()
            .setId("test")
            .setMetadata(info);
        
        //When
        List<String> actual = indexer.index(document);
        
        
        //Then
        assertThat("actual list should equal expected", actual, equalTo(expected));
    }
    
    @Test
    public void authorOnly() throws DataRepositoryException {
        //Given
        List<String> expected = Arrays.asList("fred");
        given(repo.getRevisions("test.meta")).willReturn(Arrays.asList(createDataRevision("fred")));
        GeminiDocument document = new GeminiDocument()
            .setId("test");
        
        //When
        List<String> actual = indexer.index(document);
        
        
        //Then
        assertThat("actual list should equal expected", actual, equalTo(expected));
    }
    
    @Test
    public void empty() throws DataRepositoryException {
        //Given
        List<String> expected = Collections.EMPTY_LIST;
        GeminiDocument document = new GeminiDocument()
            .setId("test");
        
        //When
        List<String> actual = indexer.index(document);
        
        
        //Then
        assertThat("actual list should equal expected", actual, equalTo(expected));
    }
    
    private DataRevision<CatalogueUser> createDataRevision(String username) {
        return new DataRevision<CatalogueUser>() {

            @Override
            public String getRevisionID() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getMessage() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getShortMessage() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public CatalogueUser getAuthor() {
                return new CatalogueUser().setUsername(username);
            }
        };
    }
    
}
