
package uk.ac.ceh.gateway.catalogue.postprocess;

import com.hp.hpl.jena.query.Dataset;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import uk.ac.ceh.gateway.catalogue.imp.Link;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

public class ImpDocumentPostProcessingServiceTest {
    private ImpDocumentPostProcessingService service;
    private Dataset jenaTdb;
    
    @Before
    public void setup() {
        jenaTdb = TDBFactory.createDataset();
        service = new ImpDocumentPostProcessingService(jenaTdb);
    }

    @Test
    public void addLinks() throws PostProcessingException {
        //Given
        Model model = new Model();
        model.setId("123");
        model.setIdentifiers(Arrays.asList("dataset1"));
        Link expected = new Link("http://dataset1", "Dataset 1");
        
        com.hp.hpl.jena.rdf.model.Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model"), IDENTIFIER, "123");
        triples.add(createResource("http://dataset1"), TITLE, "Dataset 1");
        triples.add(createResource("http://model"), REFERENCES, createResource("http://dataset1"));
        
        //When
        service.postProcess(model);
        
        //Then
        assertThat("Should have link to dataset with title: Dataset 1", model.getLinks().get(0), equalTo(expected));
    }
    
}
