package uk.ac.ceh.gateway.catalogue.postprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Link;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.services.CitationService;

/**
 *
 * @author cjohn
 */
public class GeminiDocumentPostProcessingServiceTest {
    private static final Property IDENTIFIER = ResourceFactory.createProperty("http://purl.org/dc/terms/identifier");
    private static final Property TITLE = ResourceFactory.createProperty("http://purl.org/dc/terms/title");
    private static final Property TYPE = ResourceFactory.createProperty("http://purl.org/dc/terms/type");
    private static final Property PART_OF = ResourceFactory.createProperty("http://purl.org/dc/terms/isPartOf");
    
    @Mock CitationService citationService;
    @Mock ObjectMapper mapper;
    private Dataset jenaTdb;
    private GeminiDocumentPostProcessingService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        jenaTdb = TDBFactory.createDataset();
        service = spy(new GeminiDocumentPostProcessingService(citationService, mapper, jenaTdb));
    }
    
    @Test
    public void populatesParentsLinkWhenParentIdIsPresent() throws PostProcessingException {
        //Given
        Link parent = Link.builder().build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(citationService.getCitation(document)).thenReturn(Optional.empty());
        when(document.getParentIdentifier()).thenReturn("id");
        doReturn(parent).when(service).getLink("id");
        
        //When
        service.postProcess(document);
        
        //Then
        verify(document).setParent(parent);
    }
        
    @Test
    public void checkAddsCitationInIfPresent() throws PostProcessingException {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        Citation citation = Citation.builder().build();
        when(citationService.getCitation(document)).thenReturn(Optional.of(citation));
        
        //When
        service.postProcess(document);
        
        //Then
        verify(document).setCitation(citation);
    }
    
    @Test
    public void canGetLinkFromIdentifier() {
        //Given
        Resource resource = ResourceFactory.createProperty("http://my.resource.com/uuid");
        Model model = jenaTdb.getDefaultModel();
        model.add(resource, IDENTIFIER, ResourceFactory.createPlainLiteral("uuid"));
        model.add(resource, TITLE, ResourceFactory.createPlainLiteral("title"));
        model.add(resource, TYPE, ResourceFactory.createPlainLiteral("crazy type"));
        
        //When
        Link link = service.getLink("uuid");
        
        //Then
        assertThat(link.getAssociationType(), equalTo("crazy type"));
        assertThat(link.getHref(), equalTo("http://my.resource.com/uuid"));
        assertThat(link.getTitle(), equalTo("title"));
    }
    
    @Test
    public void canGetReverseLinksFromIdentifier() {
        //Given
        Resource parent = ResourceFactory.createProperty("http://my.resource.com/parent");
        Model model = jenaTdb.getDefaultModel();
        model.add(parent, IDENTIFIER, ResourceFactory.createPlainLiteral("parent"));
        
        Resource child = ResourceFactory.createProperty("http://my.resource.com/child");
        model.add(child, PART_OF, parent);
        model.add(child, IDENTIFIER, ResourceFactory.createPlainLiteral("child"));
        model.add(child, TITLE, ResourceFactory.createPlainLiteral("child record"));
        model.add(child, TYPE, ResourceFactory.createPlainLiteral("child type"));
        
        //When
        List<Link> children = new ArrayList<>(service.getReverseLinks("parent"));
        
        //Then
        assertThat(children.size(), is(1));
        Link childLink = children.get(0);
        assertThat(childLink.getAssociationType(), equalTo("child type"));
        assertThat(childLink.getHref(), equalTo("http://my.resource.com/child"));
        assertThat(childLink.getTitle(), equalTo("child record"));
        
    }
}
