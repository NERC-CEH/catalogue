package uk.ac.ceh.gateway.catalogue.indexing.jena;

import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.jena.JenaIndexMetadataDocumentGenerator;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class JenaIndexMetadataDocumentGeneratorTest {
  @InjectMocks private JenaIndexMetadataDocumentGenerator generator;
  @Mock private DocumentIdentifierService service;

  @Test
  public void emptyIdentifierDoesNotGetIndexed() {
    //Given
    GeminiDocument document = mock(GeminiDocument.class);
    given(document.getId()).willReturn("");

    //When
    List<Statement> actual = generator.generateIndex(document);

    //Then
    assertThat("Statement list should be empty", actual.isEmpty(), equalTo(true));
  }

  @Test
  public void identifierDoesGetIndexed() {
    //Given
    GeminiDocument document = mock(GeminiDocument.class);
    given(document.getId()).willReturn("1234-5678");

    //When
    List<Statement> actual = generator.generateIndex(document);

    //Then
    assertThat("Statement list should be empty", actual.size(), equalTo(1));
    assertThat("Statement Object should be the identifier", actual.get(0).getLiteral().getString(), equalTo("1234-5678"));
  }

}
