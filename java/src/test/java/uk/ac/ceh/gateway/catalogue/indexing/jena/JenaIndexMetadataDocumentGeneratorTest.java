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

@Slf4j
@ExtendWith(MockitoExtension.class)
public class JenaIndexMetadataDocumentGeneratorTest {
  @InjectMocks private JenaIndexMetadataDocumentGenerator generator;
  @Mock private DocumentIdentifierService service;

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
    assertThat("Statement list should be empty", actual.size(), equalTo(1));
    assertThat("Statement Object should be the identifier", actual.get(0).getLiteral().getString(), equalTo("1234-5678"));
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
    assertThat(actual.size(), equalTo(2));
  }
}
