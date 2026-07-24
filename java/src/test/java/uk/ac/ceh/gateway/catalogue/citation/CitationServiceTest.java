package uk.ac.ceh.gateway.catalogue.citation;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CitationServiceTest {
    @Test
    public void getLinkToCitationWithFormat() {
        //Given
        String format = "bibtex";
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUri()).thenReturn("https://example.com/id/30ce7be8-deab-4608-bc2a-1774921423f0");
        when(document.getId()).thenReturn("30ce7be8-deab-4608-bc2a-1774921423f0");
        CitationService service = new CitationService("10.5285/");

        //When
        URI url = service.getInAlternateFormat(document, format);

        //Then
        assertThat(url.toString(), equalTo("https://example.com/documents/30ce7be8-deab-4608-bc2a-1774921423f0/citation?format=bibtex"));
    }

    @Test
    public void checkCanGenerateCitationIfDocumentMeetsRequirements() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document
            .setDatasetReferenceDate(validDate())
            .setAuthors(List.of(author()))
            .setPublishers(List.of(publisher()))
            .setTitle("document title")
            .setUri("http://document")
            .setResourceIdentifiers(List.of(nercdoi()));
        CitationService service = new CitationService("10.5285/");

        //When
        Citation citation = service.getCitation(document).orElseThrow();

        //Then
        assertNotNull(citation);
        assertEquals(1, citation.getAuthors().size());
        assertTrue(citation.getAuthors().contains("Author, A."));
        assertThat("DOI present", citation.getDoi(), equalTo("10.5285/myDoI"));
        assertThat("Title present", citation.getTitle(), equalTo("document title"));
        assertThat("Year is correct", citation.getYear(), equalTo(2000));
        assertThat("Publisher is correct", citation.getPublisher(), equalTo("Octan Corporation"));
    }

    @Test
    public void doesntCreateCitationIfNoPublishers() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUri()).thenReturn("http://document");
        when(document.getResourceIdentifiers()).thenReturn(Collections.singletonList(
            nercdoi()
        ));
        when(document.getDatasetReferenceDate()).thenReturn(validDate());
        when(document.getAuthors()).thenReturn(Collections.singletonList(
            author()
        ));
        CitationService service = new CitationService("10.5285/");

        //When
        Optional<Citation> citation = service.getCitation(document);

        //Then
        assertThat("Citation expected to be absent", citation.isPresent(), equalTo(false));
    }

    @Test
    public void doesntCreateCitationIfNoDate() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUri()).thenReturn("http://document");
        when(document.getResourceIdentifiers()).thenReturn(Collections.singletonList(
            nercdoi()
        ));
        when(document.getAuthors()).thenReturn(Arrays.asList(
            author()
        ));
        when(document.getPublishers()).thenReturn(Arrays.asList(
            publisher()
        ));
        CitationService service = new CitationService("10.5285/");

        //When
        Optional<Citation> citation = service.getCitation(document);

        //Then
        assertThat("Citation expected to be absent", citation.isPresent(), equalTo(false));
    }

    @Test
    public void doesntCreateCitationIfNotANercDOI() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUri()).thenReturn("http://document");
        when(document.getResourceIdentifiers()).thenReturn(Collections.singletonList(
            ResourceIdentifier
                .builder()
                .code("10.123456789/myDoI")
                .codeSpace("doi")
                .build()
        ));
        when(document.getAuthors()).thenReturn(Arrays.asList(
            author()
        ));
        when(document.getPublishers()).thenReturn(Arrays.asList(
            publisher()
        ));
        CitationService service = new CitationService("10.5285/");

        //When
        Optional<Citation> citation = service.getCitation(document);

        //Then
        assertThat("Citation expected to be absent", citation.isPresent(), equalTo(false));
    }

    @Test
    public void doesntCreateCitationIfNoDOI() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        CitationService service = new CitationService("10.5285/");

        //When
        Optional<Citation> citation = service.getCitation(document);

        //Then
        assertThat("Citation expected to be absent", citation.isPresent(), equalTo(false));
    }

    private ResourceIdentifier nercdoi() {
        return ResourceIdentifier
                        .builder()
                        .code("10.5285/myDoI")
                        .codeSpace("doi")
                        .build();
    }

    private DatasetReferenceDate validDate() {
        return DatasetReferenceDate
                .builder()
                .publicationDate(LocalDate.of(2000,Month.APRIL,16))
                .build();
    }

    private ResponsibleParty author() {
        return ResponsibleParty
                .builder()
                .givenName("A.")
                .familyName("Author")
                .build();
    }

    private ResponsibleParty publisher() {
        return ResponsibleParty
                .builder()
                .organisationName("Octan Corporation")
                .build();
    }
}
