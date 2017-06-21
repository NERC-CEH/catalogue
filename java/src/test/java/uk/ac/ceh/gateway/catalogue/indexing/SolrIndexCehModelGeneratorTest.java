package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;

public class SolrIndexCehModelGeneratorTest {
    @Mock SolrIndexMetadataDocumentGenerator documentIndexer;
    private SolrIndexCehModelGenerator generator;
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(documentIndexer.generateIndex(any(MetadataDocument.class))).thenReturn(new SolrIndex());
        generator = new SolrIndexCehModelGenerator(documentIndexer);
    }

    @Test
    public void scaleAddedFromModel() throws Exception {
        //Given
        CehModel model = new CehModel();
        model.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/global").value("global").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/catchment").value("catchment").build()
        ));
        
        //When
        SolrIndex index = generator.generateIndex(model);
        List<String> actual = index.getImpScale();
        
        //Then
        assertThat("Solr index should have model scale", actual, contains("global", "catchment"));
    }
    
    @Test
    public void scaleAddedFromModelApplication() throws Exception {
        //Given
        CehModelApplication application = new CehModelApplication();
        application.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/global").value("global").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/catchment").value("catchment").build()
        ));
        CehModelApplication.ModelInfo info0 = new CehModelApplication.ModelInfo();
        info0.setSpatialExtentOfApplication("plot");
        CehModelApplication.ModelInfo info1 = new CehModelApplication.ModelInfo();
        application.setModelInfos(Arrays.asList(info0, info1));
        
        //When
        SolrIndex index = generator.generateIndex(application);
        List<String> actual = index.getImpScale();
        
        //Then
        assertThat("Solr index should have model application scale", actual, contains("global", "catchment", "plot"));
    }
    
    @Test
    public void topicAddedFromModelApplication() throws Exception {
        //Given
        CehModelApplication application = new CehModelApplication();
        application.setKeywords(Arrays.asList(
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/topic/nitrogen").value("nitrogen").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/topic/management").value("management").build(),
            Keyword.builder().URI("http://vocabs.ceh.ac.uk/inms/scale/plot").value("plot").build()
        ));
        
        //When
        SolrIndex index = generator.generateIndex(application);
        List<String> actual = index.getImpTopic();
        
        //Then
        assertThat("Solr index should have model application topic", actual, contains("nitrogen", "management"));
    }
    
}
