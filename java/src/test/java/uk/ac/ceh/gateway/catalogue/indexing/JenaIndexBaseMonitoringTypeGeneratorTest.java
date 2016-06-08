package uk.ac.ceh.gateway.catalogue.indexing;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.ef.Activity;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Link;
import uk.ac.ceh.gateway.catalogue.ef.Link.TimedLink;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.SET_UP_FOR;

/**
 *
 * @author cjohn
 */
public class JenaIndexBaseMonitoringTypeGeneratorTest {
    @Mock JenaIndexMetadataDocumentGenerator generator;
    private JenaIndexBaseMonitoringTypeGenerator service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        service = new JenaIndexBaseMonitoringTypeGenerator(generator);
    }
    
    @Test
    public void checkThatCanIndexActivity() throws DocumentIndexingException {
        //Given
        Link link = mock(Link.class);
        Activity activity = mock(Activity.class);
        when(activity.getSetUpFor()).thenReturn(Arrays.asList(link));
        when(link.getHref()).thenReturn("http://tolinkTo");
        when(activity.getId()).thenReturn("activity");
        
        Resource activityResource = ResourceFactory.createResource("http://thisActivity");
        when(generator.resource("activity")).thenReturn(activityResource);
        
        //When
        List<Statement> statements = service.generateIndex(activity);
        
        //Then
        assertThat(statements.size(), is(1));
        assertThat(statements.get(0).getSubject(), is(activityResource));
        assertThat(statements.get(0).getPredicate(), is(SET_UP_FOR));
        assertThat(statements.get(0).getObject(), equalTo(ResourceFactory.createResource("http://tolinkTo")));
    }
    
    @Test
    public void checkThatCanIndexATimedLink() throws DocumentIndexingException {
        //Given
        TimedLink timedLink = mock(TimedLink.class, RETURNS_DEEP_STUBS);
        when(timedLink.getLinkingTime().getStart()).thenReturn(LocalDate.now());
        when(timedLink.getLinkingTime().getEnd()).thenReturn(LocalDate.now());
        Facility facility = mock(Facility.class);
        
        when(facility.getId()).thenReturn("my id");
        when(generator.resource("my id")).thenReturn(ResourceFactory.createResource("http://thisActivity"));
        when(facility.getBelongsTo()).thenReturn(Arrays.asList(timedLink));
        
        //When
        List<Statement> statements = service.generateIndex(facility);
        
        //Then
        assertThat(statements.size(), is(5));
    }
    
    @Test
    public void checkThatTimedLinkWithNoLifeSpanGetsIndexLikeNormalLink() throws DocumentIndexingException {
        //Given
        TimedLink timedLink = mock(TimedLink.class, RETURNS_DEEP_STUBS);
        Facility facility = mock(Facility.class);
        
        when(facility.getId()).thenReturn("my id");
        when(generator.resource("my id")).thenReturn(ResourceFactory.createResource("http://thisActivity"));
        when(facility.getBelongsTo()).thenReturn(Arrays.asList(timedLink));
        
        //When
        List<Statement> statements = service.generateIndex(facility);
        
        //Then
        assertThat(statements.size(), is(1));
    }
}
