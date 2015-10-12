package uk.ac.ceh.gateway.catalogue.postprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.*;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import static org.mockito.Matchers.any;
import uk.ac.ceh.gateway.catalogue.gemini.Link;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;

/**
 *
 * @author cjohn
 */
public class GeminiDocumentPostProcessingServiceTest {    
    @Mock CitationService citationService;
    @Mock DataciteService dataciteService;
    @Mock ObjectMapper mapper;
    @Captor ArgumentCaptor<Set<Link>> links;
    private Dataset jenaTdb;
    private GeminiDocumentPostProcessingService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        jenaTdb = TDBFactory.createDataset();
        service = spy(new GeminiDocumentPostProcessingService(citationService, dataciteService, mapper, jenaTdb));
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
    public void checkThatCanScanLinksToFindParent() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://doc1"), IDENTIFIER, "child");
        triples.add(createResource("http://doc1"), IS_PART_OF, "parent");
        triples.add(createResource("http://doc2"), IDENTIFIER, "parent");
        triples.add(createResource("http://doc2"), TITLE, "Daddy");
        triples.add(createResource("http://doc2"), TYPE, "Person");
        
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn("child");
        
        //When
        service.postProcess(document);
        
        //Then
        ArgumentCaptor<Link> parent = ArgumentCaptor.forClass(Link.class);
        verify(document).setParent(parent.capture());
        assertThat(parent.getValue().getHref(), equalTo("http://doc2"));
        assertThat(parent.getValue().getTitle(), equalTo("Daddy"));
        assertThat(parent.getValue().getAssociationType(), equalTo("Person"));
    }
    
    @Test
    public void checkThatCanFindChildren() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://doc1"), IDENTIFIER, "child");
        triples.add(createResource("http://doc1"), TITLE, "Baby");
        triples.add(createResource("http://doc1"), TYPE, "person");
        triples.add(createResource("http://doc1"), IS_PART_OF, "parent");
        triples.add(createResource("http://doc2"), IDENTIFIER, "parent");
        
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn("parent");
        
        //When
        service.postProcess(document);
        
        //Then
        verify(document).setChildren(links.capture());
        assertThat(links.getValue().size(), is(1));
        assertThat(links.getValue(), hasItem(Link.builder()
                .associationType("person")
                .title("Baby")
                .href("http://doc1")
                .build()));
    }
    
    @Test
    public void checkThatCanFindDocumentsLinkedTo() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://man"), IDENTIFIER, "manchester");
        triples.add(createResource("http://man"), TITLE, "Big City");
        triples.add(createResource("http://man"), TYPE, "CITY");
        triples.add(createResource("http://altrincham"), IDENTIFIER, "altrincham");
        triples.add(createResource("http://altrincham"), RELATION, "manchester");
                
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn("altrincham");
        
        //When
        service.postProcess(document);
        
        //Then
        verify(document).setDocumentLinks(links.capture());
        assertThat(links.getValue().size(), is(1));
        assertThat(links.getValue(), hasItem(Link.builder()
                .associationType("CITY")
                .title("Big City")
                .href("http://man")
                .build()));
    }
       
    @Test
    public void checkThatCanFindDocumentsLinkedFrom() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://man"), IDENTIFIER, "manchester");
        triples.add(createResource("http://altrincham"), IDENTIFIER, "altrincham");
        triples.add(createResource("http://altrincham"), RELATION, "manchester");
        triples.add(createResource("http://altrincham"), TITLE, "Town");
        triples.add(createResource("http://altrincham"), TYPE, "TOWN");
                
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn("manchester");
        
        //When
        service.postProcess(document);
        
        //Then
        verify(document).setDocumentLinks(links.capture());
        assertThat(links.getValue().size(), is(1));
        assertThat(links.getValue(), hasItem(Link.builder()
                .associationType("TOWN")
                .title("Town")
                .href("http://altrincham")
                .build()));
    }
}
