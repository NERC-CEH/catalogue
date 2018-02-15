package uk.ac.ceh.gateway.catalogue.indexing;

import org.apache.jena.rdf.model.Statement;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

public class JenaIndexMetadataDocumentGeneratorTest {
  private JenaIndexMetadataDocumentGenerator generator;
  @Mock private DocumentIdentifierService service;
  
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    generator = new JenaIndexMetadataDocumentGenerator(service);
  }

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
