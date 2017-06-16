package uk.ac.ceh.gateway.catalogue.indexing;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

public class SolrIndexImpDocumentGeneratorTest {
    @Mock SolrIndexMetadataDocumentGenerator documentIndexer;
    private SolrIndexImpDocumentGenerator generator;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(documentIndexer.generateIndex(any(MetadataDocument.class))).thenReturn(new SolrIndex());
        generator = new SolrIndexImpDocumentGenerator(documentIndexer);
    }

    @Test
    public void applicationScaleAddedToIndex() throws Exception {
        //Given
        Model document = new Model();
        document.setApplicationScale("global");
        
        //When
        SolrIndex actual = generator.generateIndex(document);
        
        //Then
        assertThat("applicationScale transferred to index", actual.getImpScale(), contains("global"));
    }
    
}
