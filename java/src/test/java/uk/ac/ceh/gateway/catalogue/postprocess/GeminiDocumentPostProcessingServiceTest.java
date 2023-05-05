package uk.ac.ceh.gateway.catalogue.postprocess;

import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.citation.CitationService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;

import java.util.Optional;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

@ExtendWith(MockitoExtension.class)
public class GeminiDocumentPostProcessingServiceTest {
    @Mock CitationService citationService;
    @Mock DataciteService dataciteService;
    @Mock DocumentIdentifierService documentIdentifierService;
    private Dataset jenaTdb;
    private GeminiDocumentPostProcessingService service;

    @BeforeEach
    public void init() {
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        jenaTdb = TDBFactory.createDataset();
        service = new GeminiDocumentPostProcessingService(citationService, dataciteService, jenaTdb, documentIdentifierService);
    }


    @Test
    public void checkAddsCitationInIfPresent() throws PostProcessingException {
        //Given
        val document = new GeminiDocument();
        document.setId("abc");
        Citation citation = Citation.builder().build();
        when(citationService.getCitation(document)).thenReturn(Optional.of(citation));
        when(documentIdentifierService.generateUri(any(String.class))).thenReturn("123");

        //When
        service.postProcess(document);

        //Then
        assertThat(document.getCitation(), is(notNullValue()));
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
