package uk.ac.ceh.gateway.catalogue.postprocess;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import uk.ac.ceh.gateway.catalogue.model.Link;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

public class GeminiDocumentPostProcessingServiceTest {    
 /*    @Mock CitationService citationService;
    @Mock DataciteService dataciteService;
    @Mock DocumentIdentifierService documentIdentifierService;
    private Dataset jenaTdb;
    private GeminiDocumentPostProcessingService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        jenaTdb = TDBFactory.createDataset();
        service = new GeminiDocumentPostProcessingService(citationService, dataciteService, jenaTdb, documentIdentifierService);
    }
    
    @Ignore
    @Test
    public void checkAddsCitationInIfPresent() throws PostProcessingException {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        Citation citation = Citation.builder().build();
        when(citationService.getCitation(document)).thenReturn(Optional.of(citation));
        when(documentIdentifierService.generateUri(any(String.class))).thenReturn("123");
        
        //When
        service.postProcess(document);
        
        //Then
        verify(document).setCitation(citation);
    }
    
    @Test
    public void checkThatCanScanLinksToFindParent() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://doc1"), IS_PART_OF, createResource("http://doc2"));
        triples.add(createResource("http://doc2"), TITLE, "Daddy");
        triples.add(createResource("http://doc2"), TYPE, "Person");
        
        GeminiDocument document = new GeminiDocument();
        document.setId("doc1");
        when(documentIdentifierService.generateUri("doc1")).thenReturn("http://doc1");
        
        //When
        service.postProcess(document);
        
        //Then
        assertThat(document.getParent().getHref(), equalTo("http://doc2"));
        assertThat(document.getParent().getTitle(), equalTo("Daddy"));
        assertThat(document.getParent().getAssociationType(), equalTo("Person"));
    }
    
    @Ignore
    @Test
    public void checkThatCanFindChildren() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://doc1"), TITLE, "Baby");
        triples.add(createResource("http://doc1"), TYPE, "person");
        triples.add(createResource("http://doc1"), IS_PART_OF, createResource("http://doc2"));
        triples.add(createResource("http://doc2"), TYPE, "series");
        
        GeminiDocument document = new GeminiDocument();
        document.setId("doc2");
        when(documentIdentifierService.generateUri("doc2")).thenReturn("http://doc2");
        
        //When
        service.postProcess(document);
        
        //Then
        assertThat(document.getChildren().size(), is(1));
        assertThat(document.getChildren(), hasItem(Link.builder()
                .associationType("person")
                .title("Baby")
                .href("http://doc1")
                .rel("http://purl.org/dc/terms/isPartOf")
                .build()));
    }
    
    @Ignore
    @Test
    public void checkThatCanFindDocumentsLinkedTo() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://man"), TITLE, "Big City");
        triples.add(createResource("http://man"), TYPE, "CITY");
        triples.add(createResource("http://altrincham"), RELATION, createResource("http://man"));
                
        GeminiDocument document = new GeminiDocument();
        document.setId("altrincham");
        when(documentIdentifierService.generateUri("altrincham")).thenReturn("http://altrincham");
        
        //When
        service.postProcess(document);
        
        //Then
        assertThat(document.getDocumentLinks().size(), is(1));
        assertThat(document.getDocumentLinks(), hasItem(Link.builder()
                .associationType("CITY")
                .title("Big City")
                .href("http://man")
                .rel("http://purl.org/dc/terms/relation")
                .build()));
    }
       
    @Ignore
    @Test
    public void checkThatCanFindDocumentsLinkedFrom() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://altrincham"), RELATION, createResource("http://man"));
        triples.add(createResource("http://altrincham"), TITLE, "Town");
        triples.add(createResource("http://altrincham"), TYPE, "TOWN");
                
        GeminiDocument document = new GeminiDocument();
        document.setId("man");
        when(documentIdentifierService.generateUri("man")).thenReturn("http://man");
        
        //When
        service.postProcess(document);
        
        //Then
        assertThat(document.getDocumentLinks().size(), is(1));
        assertThat(document.getDocumentLinks(), hasItem(Link.builder()
                .associationType("TOWN")
                .title("Town")
                .href("http://altrincham")
                .rel("http://purl.org/dc/terms/relation")
                .build()));
    }
   
    @Ignore
    @Test
    public void checkCanFindModelLinks() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model"), REFERENCES, createResource("http://dataset"));
        triples.add(createResource("http://model"), TITLE, "Model");
        triples.add(createResource("http://model"), TYPE, "model");
                
        GeminiDocument document = new GeminiDocument();
        document.setId("dataset");
        when(documentIdentifierService.generateUri("dataset")).thenReturn("http://dataset");
        
        //When
        service.postProcess(document);
        
        //Then
        assertThat(document.getModelLinks().size(), is(1));
        assertThat(document.getModelLinks(), hasItem(Link.builder()
                .associationType("model")
                .title("Model")
                .href("http://model")
                .rel("http://purl.org/dc/terms/references")
                .build()));
    } */
}
