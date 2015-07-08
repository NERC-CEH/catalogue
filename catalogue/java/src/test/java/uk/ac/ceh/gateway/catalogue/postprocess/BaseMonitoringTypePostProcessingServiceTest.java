package uk.ac.ceh.gateway.catalogue.postprocess;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createStatement;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.util.Calendar;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.joda.time.LocalDate;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Link;
import uk.ac.ceh.gateway.catalogue.ef.Link.TimedLink;
import uk.ac.ceh.gateway.catalogue.ef.Metadata;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.BROADER;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.IDENTIFIER;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.LINKING_TIME;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.TEMPORAL_BEGIN;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.TEMPORAL_END;
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
    
    @Test
    public void checkThatCanFindLinkWithoutTitle() {
        //Given
        Metadata metadata = mock(Metadata.class);
        when(metadata.getSelfUrl()).thenReturn("https://my.activity");
        Activity activity = new Activity();
        activity.setEfMetadata(metadata);
        
        Resource knownLink = ResourceFactory.createResource("https://linkedTo");
        tripleStore.add(knownLink, TRIGGERS, ResourceFactory.createResource("https://my.activity"));
        
        //When
        service.postProcess(activity);
        
        //Then
        List<Link> setupfor = activity.getSetUpFor();
        assertThat(setupfor.size(), is(1));
    }
    
    @Test
    public void checkThatCanReadTimedLink() throws DocumentIndexingException {
        //Given
        Calendar start = Calendar.getInstance(); start.set(2000, 3, 2);
        Calendar end = Calendar.getInstance();   end.set(2020, 3, 2);
        Metadata metadata = mock(Metadata.class);
        when(metadata.getSelfUrl()).thenReturn("https://my.activity");
        Facility facility = new Facility();
        facility.setEfMetadata(metadata);
        
        Resource timedLinkUri = createResource();
        tripleStore.add(createStatement(timedLinkUri, IDENTIFIER, ResourceFactory.createResource("https://linkedTo"))); // Link timed node to actual node
        tripleStore.add(createStatement(ResourceFactory.createResource("https://linkedTo"), TITLE, ResourceFactory.createPlainLiteral("resource title"))); // Link timed node to actual node
        Resource linkingTime = createResource();
        tripleStore.add(createStatement(linkingTime, TEMPORAL_BEGIN, createTypedLiteral(start)));
        tripleStore.add(createStatement(linkingTime, TEMPORAL_END, createTypedLiteral(end)));
        tripleStore.add(createStatement(timedLinkUri, LINKING_TIME, linkingTime));
        //Set up the relationship between my activity and the timed link
        tripleStore.add(createStatement(timedLinkUri, BROADER, ResourceFactory.createResource("https://my.activity"))); 
        
        //When
        service.postProcess(facility);
        
        //Then
        List<TimedLink> narrower = facility.getNarrowerThan();
        assertThat(narrower.size(), is(1));
        TimedLink link = narrower.get(0);
        assertThat(link.getHref(), equalTo("https://linkedTo"));
        assertThat(link.getTitle(), equalTo("resource title"));
        assertThat(link.getLinkingTime().getStart(), equalTo(LocalDate.fromCalendarFields(start)));
        assertThat(link.getLinkingTime().getEnd(), equalTo(LocalDate.fromCalendarFields(end)));
    }
}
