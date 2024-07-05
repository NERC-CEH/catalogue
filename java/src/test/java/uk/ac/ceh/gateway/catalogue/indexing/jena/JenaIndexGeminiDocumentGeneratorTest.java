package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JenaIndexGeminiDocumentGeneratorTest {

    @Mock private DocumentIdentifierService service;
    private JenaIndexGeminiDocumentGenerator generator;

    @BeforeEach
    public void setup() {
        String baseURI = "https://example.com";
        generator = new JenaIndexGeminiDocumentGenerator(new JenaIndexMetadataDocumentGenerator(service), baseURI);
    }

    @Test
    void blankStringResourceIdentifiersNotIndexed() {
        //Given
        val document = new GeminiDocument();
        document.setId("t");
        document.setResourceIdentifiers(List.of(ResourceIdentifier.builder().build()));
        given(service.generateUri("t")).willReturn("t");

        //When
        List<Statement> actual = generator.generateIndex(document);

        //Then
        assertThat("Should be two identifier statements", actual.size(), equalTo(5));
        assertThat("Statement literal should be identifier", actual.get(0).getLiteral().getString(), equalTo("t"));
        assertThat("Statement literal should be status", actual.get(1).getLiteral().getString(), equalTo("draft"));
        assertThat("Statement literal should be identifier", actual.get(2).getLiteral().getString(), equalTo("t"));
        assertThat("Statement literal should be available", actual.get(3).getLiteral().getString(), equalTo(LocalDate.now().toString()));
        assertThat("Statement literal should be status", actual.get(4).getLiteral().getString(), equalTo("Unknown"));
        // No resource identifiers added to statements
    }
}
