package uk.ac.ceh.gateway.catalogue.postprocess;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.citation.Citation;
import uk.ac.ceh.gateway.catalogue.citation.CitationService;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeminiDocumentPostProcessingServiceTest {
    @Mock CitationService citationService;
    @Mock DataciteService dataciteService;
    private GeminiDocumentPostProcessingService service;

    @BeforeEach
    public void init() {
        when(citationService.getCitation(any(GeminiDocument.class))).thenReturn(Optional.empty());
        service = new GeminiDocumentPostProcessingService(citationService, dataciteService);
    }


    @Test
    public void checkAddsCitationInIfPresent() {
        //Given
        val document = new GeminiDocument();
        document.setId("abc");
        Citation citation = Citation.builder().build();
        when(citationService.getCitation(document)).thenReturn(Optional.of(citation));

        //When
        service.postProcess(document);

        //Then
        assertThat(document.getCitation(), is(notNullValue()));
    }
}
