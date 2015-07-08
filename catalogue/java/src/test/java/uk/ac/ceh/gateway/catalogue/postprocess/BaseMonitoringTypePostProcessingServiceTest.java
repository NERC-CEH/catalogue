package uk.ac.ceh.gateway.catalogue.postprocess;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.Link;
import uk.ac.ceh.gateway.catalogue.ef.Metadata;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.TITLE;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.TRIGGERS;

/**
 *
 * @author cjohn
 */
public class BaseMonitoringTypePostProcessingServiceTest {
    private Model tripleStore;
    private BaseMonitoringTypePostProcessingService service;
    
    @Before
    public void init() {
        Dataset dataset = TDBFactory.createDataset();
        this.tripleStore = dataset.getDefaultModel();
        service = new BaseMonitoringTypePostProcessingService(dataset);
    }
    
    @Test
    public void checkThatAddsOutgoingLinksForActivity() {
        //Given
        Metadata metadata = mock(Metadata.class);
        when(metadata.getSelfUrl()).thenReturn("https://my.activity");
        Activity activity = new Activity();
        activity.setEfMetadata(metadata);
        
        Resource knownLink = ResourceFactory.createResource("https://linkedTo");
        tripleStore.add(knownLink, TITLE, ResourceFactory.createPlainLiteral("Link from some other document"));
        tripleStore.add(knownLink, TRIGGERS, ResourceFactory.createResource("https://my.activity"));
        
        //When
        service.postProcess(activity);
        
        //Then
        List<Link> setupfor = activity.getSetUpFor();
        assertThat(setupfor.size(), is(1));
    }
    
    @Test
    public void checkThatFavoursExistingLinks() {
        //Given
        Metadata metadata = mock(Metadata.class);
        when(metadata.getSelfUrl()).thenReturn("https://my.activity");
        Activity activity = new Activity();
        activity.setEfMetadata(metadata);
        Link existingLink = new Link().setHref("https://linkedTo");
        activity.getSetUpFor().add(existingLink);
        
        Resource knownLink = ResourceFactory.createResource("https://linkedTo");
        tripleStore.add(knownLink, TITLE, ResourceFactory.createPlainLiteral("Link from some other document"));
        tripleStore.add(knownLink, TRIGGERS, ResourceFactory.createResource("https://my.activity"));
        
        //When
        service.postProcess(activity);
        
        //Then
        List<Link> setupfor = activity.getSetUpFor();
        assertThat(setupfor.size(), is(1));
        assertThat(setupfor.get(0), is(existingLink));
    }
}
