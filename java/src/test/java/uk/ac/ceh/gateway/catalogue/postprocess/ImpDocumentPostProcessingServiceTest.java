
package uk.ac.ceh.gateway.catalogue.postprocess;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import uk.ac.ceh.gateway.catalogue.imp.Link;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import org.junit.Ignore;

@Slf4j
public class ImpDocumentPostProcessingServiceTest {
    private ImpDocumentPostProcessingService service;
    private Dataset jenaTdb;
    
    @Before
    public void setup() {
        jenaTdb = TDBFactory.createDataset();
        service = new ImpDocumentPostProcessingService(jenaTdb);
    }

    @Test
    @Ignore
    public void addLinks() throws PostProcessingException {
        //Given
//        Model model = new Model();
//        model.setUri("http://model");
//        model.setIdentifiers(Arrays.asList("dataset1"));
//        Link expected = new Link("http://dataset1", "Dataset 1");
//        
//        org.apache.jena.rdf.model.Model triples = jenaTdb.getDefaultModel();
//        triples.add(createResource("http://dataset1"), TITLE, "Dataset 1");
//        triples.add(createResource("http://model"), REFERENCES, createResource("http://dataset1"));
        
        //When
//        service.postProcess(model);
//        log.info("links: {}", model.getLinks());
        
        //Then
//        assertThat("Links should not be null", model.getLinks(), not(nullValue()));
//        assertThat("Should have link to dataset with title: Dataset 1", model.getLinks().get(0), equalTo(expected));
    }
    
}
