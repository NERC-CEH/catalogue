package uk.ac.ceh.gateway.catalogue.indexing.jena;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.tdb.TDBFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JenaIndexingServiceTest {

    @Mock BundledReaderService reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository repo;
    @Mock
    IndexGenerator indexGenerator;
    @Mock DocumentIdentifierService documentIdentifierService;
    private Dataset jenaTdb;
    private JenaIndexingService service;

    @BeforeEach
    public void init() {
        jenaTdb = TDBFactory.createDataset();
        service = spy(new JenaIndexingService(reader, listingService, repo, indexGenerator, documentIdentifierService, jenaTdb));
    }

    @Test
    public void checkThatCanDetectAnEmptyModel() throws DocumentIndexingException {
        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertThat(isEmpty, is(true));
    }

    @Test
    public void canCheckThatTheModelHasTriplesIn() throws DocumentIndexingException, Exception {
        //Given
        Resource subject = ResourceFactory.createResource("http://www.google.com");
        Property predicate = ResourceFactory.createProperty("http://www.google.com");
        Resource object = ResourceFactory.createResource("http://www.google.com");
        service.index(Arrays.asList(ResourceFactory.createStatement(subject, predicate, object)));

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertThat(isEmpty, is(false));
    }

    @Test
    public void canCheckThatTheEmptyTheModel() throws DocumentIndexingException, Exception {
        //Given
        Resource subject = ResourceFactory.createResource("http://www.google.com");
        Property predicate = ResourceFactory.createProperty("http://www.google.com");
        Resource object = ResourceFactory.createResource("http://www.google.com");
        service.index(Arrays.asList(ResourceFactory.createStatement(subject, predicate, object)));

        //When
        service.clearIndex();

        //Then
        assertThat(service.isIndexEmpty(), is(true));
    }

    @Test
    public void checkThatCanUnindexSubjectTriples() throws Exception {
        //Given
        String subjectUri = "http://www.ceh.ac.uk/removeMe";
        Resource subject = ResourceFactory.createResource(subjectUri);
        Property predicate = ResourceFactory.createProperty("http://ceh.ac.uk/property");
        Resource object = ResourceFactory.createResource("http://ceh.ac.uk/linkedId");
        service.index(Arrays.asList(ResourceFactory.createStatement(subject, predicate, object)));

        when(documentIdentifierService.generateUri("removeMe")).thenReturn(subjectUri);

        //When
        service.unindexDocuments(Arrays.asList("removeMe"));

        //Then
        assertThat(service.isIndexEmpty(), is(true));
    }

    @Test
    public void checkThatCannotUnindexObjectTriples() throws Exception {
        /*
        Do not want to remove Object triples as they have been asserted by
        another resource.
        */
        //Given
        String objectUri = "http://www.ceh.ac.uk/removeMe";
        Resource subject = ResourceFactory.createResource("http://www.external.com/subject");
        Property predicate = ResourceFactory.createProperty("http://ceh.ac.uk/property");
        Resource object = ResourceFactory.createResource(objectUri);
        service.index(Arrays.asList(ResourceFactory.createStatement(subject, predicate, object)));

        when(documentIdentifierService.generateUri("removeMe")).thenReturn(objectUri);

        //When
        service.unindexDocuments(Arrays.asList("removeMe"));

        //Then
        assertThat(service.isIndexEmpty(), is(false));
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
    public void checkThatisInTransactionIsThreadSpecific() throws Exception {
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
