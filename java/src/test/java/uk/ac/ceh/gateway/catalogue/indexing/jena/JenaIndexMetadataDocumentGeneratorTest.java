package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Relationship;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class JenaIndexMetadataDocumentGeneratorTest {
    @InjectMocks private JenaIndexMetadataDocumentGenerator generator;
    @Mock private DocumentIdentifierService service;

    @Test
    void generateResourceFromId() {
        //given
        val id = "123-456-789";
        given(service.generateUri(id)).willReturn("https://example.com/123-456-789");

        //when
        generator.resource(id);

        //then
        verify(service).generateUri(id);
    }

    @Test
    void generateResourceFromUri() {
        //given
        val uri = "https://example.som/123-456-789";

        //when
        generator.resource(uri);

        //then
        verifyNoInteractions(service);
    }
    @Test
    void emptyIdentifierDoesNotGetIndexed() {
        //Given
        val document = new GeminiDocument();
        document.setId("");

        //When
        List<Statement> actual = generator.generateIndex(document);

        //Then
        assertThat("Statement list should be empty", actual.isEmpty(), equalTo(true));
    }

    @Test
    void identifierDoesGetIndexed() {
        //Given
        val document = new GeminiDocument();
        document.setId("1234-5678");

        //When
        List<Statement> actual = generator.generateIndex(document);

        //Then
        assertThat("Statement list should have 2 entries", actual.size(), equalTo(2));
        assertThat("Statement Object should be the identifier", actual.get(0).getLiteral().getString(), equalTo("1234-5678"));
        assertThat("Statement Object should be the status", actual.get(1).getLiteral().getString(), equalTo("draft"));
    }

    @Test
    void relationshipsIndexed() {
        //given
        val document = new GeminiDocument();
        document.setId("1234-5678");
        document.setRelationships(Set.of(
                new Relationship("https://vocabs.ceh.ac.uk/eidc#uses", "https://example.com/12")
        ));

        //when
        val actual = generator.generateIndex(document);
        log.info("statements: {}", actual);

        //then
        assertThat(actual.size(), equalTo(3));
    }
}
