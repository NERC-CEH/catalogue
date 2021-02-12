package uk.ac.ceh.gateway.catalogue.postprocess;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import uk.ac.ceh.gateway.catalogue.model.Link;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

public class GeminiDocumentPostProcessingServiceTest {    
    @Mock CitationService citationService;
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
    public void checkThatCanFindRelatedRecords() throws PostProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://doc1"), TITLE, "Dataset 1");
        triples.add(createResource("http://doc1"), TYPE, "dataset");
        triples.add(createResource("http://doc1"), ANYREL, createResource("http://doc2"));
        triples.add(createResource("http://doc2"), TYPE, "series");
        
        GeminiDocument document = new GeminiDocument();
        document.setId("doc2");
        when(documentIdentifierService.generateUri("doc2")).thenReturn("http://doc2");
        
        //When
        service.postProcess(document);
        
        //Then
        assertThat(document.getIncomingRelationships().size(), is(1));
        assertThat(document.getIncomingRelationships(), hasItem(Link.builder()
                .associationType("dataset")
                .title("Dataset 1")
                .href("http://doc1")
                .rel("https://vocabs.ceh.ac.uk/eidc#")
                .build()));
    }

}
