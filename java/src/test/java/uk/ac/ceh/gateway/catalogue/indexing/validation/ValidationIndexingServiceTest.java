package uk.ac.ceh.gateway.catalogue.indexing.validation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ValidationIndexingServiceTest {

    @Mock private BundledReaderService<MetadataDocument> reader;
    @Mock private DocumentListingService listingService;
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private PostProcessingService<GeminiDocument> postProcessingService;
    @Mock private DocumentIdentifierService documentIdentifierService;
    @Mock private IndexGenerator<GeminiDocument, ValidationReport> indexGenerator;

    @InjectMocks private ValidationIndexingService service;

    @Test
    void checkThatIndexEmptyIsAtStart() {
        //Given
        //Nothing

        //When
        boolean isEmpty = service.isIndexEmpty();

        //Then
        assertTrue(isEmpty);
    }

    @Test
    void checkThatFailedIndexGetsCaught() throws Exception {
        //Given
        String rev = "revision";
        List<String> docs = Arrays.asList("1","2");
        given(reader.readBundle(anyString(), eq(rev))).willReturn(new GeminiDocument());
        given(indexGenerator.generateIndex(any(GeminiDocument.class)))
            .willThrow(new DocumentIndexingException("I failed"))
            .willReturn(new ValidationReport("2"));

        //When
        assertThrows(DocumentIndexingException.class, () ->
                service.indexDocuments(docs, rev)
                );

        //Then
        assertFalse(service.isIndexEmpty());
        assertTrue(service.getFailed().contains("1"));
        assertEquals("2", service.getResults().get(0).getDocumentId());
    }
}
