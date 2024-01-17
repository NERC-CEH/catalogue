package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.tdb.TDBFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JenaIndexingServiceTest {

    @Mock BundledReaderService<MetadataDocument> reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository<CatalogueUser> repo;
    @Mock IndexGenerator<MetadataDocument, List<Statement>> indexGenerator;
    @Mock DocumentIdentifierService documentIdentifierService;
    private Dataset jenaTdb;
    private JenaIndexingService service;

    @BeforeEach
    public void init() {
        jenaTdb = TDBFactory.createDataset();
        service = spy(new JenaIndexingService(reader, listingService, repo, indexGenerator, documentIdentifierService, jenaTdb));
    }

    @Test
    @SneakyThrows
    void serviceAgreementNotIndexed() {
        //given
        String revId = "Latest";
        List<String> documents = List.of("serviceAgreement1");
        val serviceAgreement = new ServiceAgreement();
        given(reader.readBundle("serviceAgreement1"))
            .willReturn(serviceAgreement);
        given(reader.readBundle("serviceAgreement1", revId))
            .willReturn(serviceAgreement);

        //when
        service.indexDocuments(documents, revId);

        //then
        verify(indexGenerator, never()).generateIndex(any(MetadataDocument.class));
    }

    @Test
    void checkThatCanDetectAnEmptyModel() throws DocumentIndexingException {
        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertTrue(isEmpty);
    }

    @Test
    void canCheckThatTheModelHasTriplesIn() throws Exception {
        //Given
        Resource subject = ResourceFactory.createResource("http://www.google.com");
        Property predicate = ResourceFactory.createProperty("http://www.google.com");
        Resource object = ResourceFactory.createResource("http://www.google.com");
        service.index(List.of(ResourceFactory.createStatement(subject, predicate, object)));

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertFalse(isEmpty);
    }

    @Test
    void canCheckThatTheEmptyTheModel() throws Exception {
        //Given
        Resource subject = ResourceFactory.createResource("http://www.google.com");
        Property predicate = ResourceFactory.createProperty("http://www.google.com");
        Resource object = ResourceFactory.createResource("http://www.google.com");
        service.index(List.of(ResourceFactory.createStatement(subject, predicate, object)));

        //When
        service.clearIndex();

        //Then
        assertTrue(service.isIndexEmpty());
    }

    @Test
    void checkThatCanUnindexSubjectTriples() throws Exception {
        //Given
        val subjectUri = "https://www.ceh.ac.uk/removeMe";
        val subject = ResourceFactory.createResource(subjectUri);
        val predicate = ResourceFactory.createProperty("https://ceh.ac.uk/property");
        val object = ResourceFactory.createResource("https://ceh.ac.uk/linkedId");
        service.index(List.of(ResourceFactory.createStatement(subject, predicate, object)));

        given(documentIdentifierService.generateUri("removeMe"))
            .willReturn(subjectUri);

        given(reader.readBundle("removeMe"))
            .willReturn(new GeminiDocument());

        //When
        service.unindexDocuments(List.of("removeMe"));

        //Then
        assertTrue(service.isIndexEmpty());
    }

    @Test
    void checkThatCannotUnindexObjectTriples() throws Exception {
        /*
         * Do not want to remove Object triples as they have been asserted by
         * another resource.
         */

        //Given
        val objectUri = "https://www.ceh.ac.uk/removeMe";
        val subject = ResourceFactory.createResource("https://www.external.com/subject");
        val predicate = ResourceFactory.createProperty("https://ceh.ac.uk/property");
        val object = ResourceFactory.createResource(objectUri);
        service.index(List.of(ResourceFactory.createStatement(subject, predicate, object)));

        given(documentIdentifierService.generateUri("removeMe"))
            .willReturn(objectUri);

        given(reader.readBundle("removeMe"))
            .willReturn(new GeminiDocument());

        //When
        service.unindexDocuments(List.of("removeMe"));

        //Then
        assertFalse(service.isIndexEmpty());
    }

    @Test
    public void checkThatUnindexesDocumentsBeforeIndexing() throws DocumentIndexingException {
        //Given
        List<String> docs = new ArrayList<>();

        //When
        service.indexDocuments(docs, "some revision");

        //Then
        verify(service).unindexDocuments(docs);
    }

    @Test
    public void checkThatIsInTransactionIsThreadSpecific() throws Exception {
        //Given
        jenaTdb.begin(ReadWrite.WRITE);

        //When
        boolean otherThreadInTransaction = Executors
                .newSingleThreadExecutor()
                .submit( () -> jenaTdb.isInTransaction())
                .get();

        boolean thisThreadInTransaction = jenaTdb.isInTransaction();

        //Then
        assertThat(otherThreadInTransaction, is(false));
        assertThat(thisThreadInTransaction, is(true));
    }
}
