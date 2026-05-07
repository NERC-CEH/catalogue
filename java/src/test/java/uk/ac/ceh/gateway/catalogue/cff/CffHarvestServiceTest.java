package uk.ac.ceh.gateway.catalogue.cff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CffHarvestServiceTest {

    private RestTemplate restTemplate;
    private CffHarvestService service;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        service = new CffHarvestService(restTemplate);
    }

    @Test
    void shouldMapBasicFieldsFromCff() {
        String yaml = """
            title: "Test Project"
            abstract: "A sample abstract"
            type: "software"
            doi: "10.1234/example"
            license: "MIT"
            """;

        when(restTemplate.getForObject("https://raw.githubusercontent.com/user/repo/main/CITATION.cff", String.class))
            .thenReturn(yaml);

        GeminiDocument doc = service.createGeminiFromCff("https://github.com/user/repo/blob/main/CITATION.cff");

        assertEquals("Test Project", doc.getTitle());
        assertEquals("A sample abstract", doc.getDescription());
        assertEquals("application", doc.getResourceType().getValue());
        assertEquals("10.1234/example", doc.getResourceIdentifiers().getFirst().getCode());
        assertEquals("MIT", doc.getUseConstraints().getFirst().getValue());
    }

    @Test
    void shouldHandleEmptyYamlResponse() {
        when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(String.class)))
            .thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.createGeminiFromCff("https://github.com/user/repo/blob/main/CITATION.cff"));

        assertTrue(ex.getMessage().contains("Empty response"));
    }

    @Test
    void shouldHandleAuthors() {
        String yaml = """
            title: "Test Project"
            authors:
              - given-names: "Alice"
                family-names: "Smith"
                email: "alice@example.com"
                affiliation: "UKCEH"
                orcid: "0000-0001-2345-6789"
            """;

        when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(String.class)))
            .thenReturn(yaml);

        GeminiDocument doc = service.createGeminiFromCff("https://github.com/user/repo/blob/main/CITATION.cff");

        assertNotNull(doc.getResponsibleParties());
        assertEquals(1, doc.getResponsibleParties().size());
        assertEquals("Alice Smith", doc.getResponsibleParties().get(0).getDisplayName());
        assertEquals("author", doc.getResponsibleParties().get(0).getRole());
    }
}
