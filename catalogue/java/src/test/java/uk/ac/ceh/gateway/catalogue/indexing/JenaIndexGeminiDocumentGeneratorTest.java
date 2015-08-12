/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Statement;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

/**
 *
 * @author rjsc
 */
public class JenaIndexGeminiDocumentGeneratorTest {
  @Mock private DocumentIdentifierService service;
  private JenaIndexGeminiDocumentGenerator generator;
  
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    generator = new JenaIndexGeminiDocumentGenerator(new JenaIndexMetadataDocumentGenerator(service));
  }
  
  @Test
  public void blankStringResourceIdentifiersNotIndexed() {
    //Given
    GeminiDocument document = mock(GeminiDocument.class);
    given(service.generateUri("t")).willReturn("t");
    given(document.getId()).willReturn("t");
    given(document.getResourceIdentifiers()).willReturn(Arrays.asList(ResourceIdentifier.builder().build()));
    
    //When
    List<Statement> actual = generator.generateIndex(document);
    
    //Then
    assertThat("Should be two identifier statements", actual.size(), equalTo(2));
    assertThat("Statement literal should be identifier", actual.get(0).getLiteral().getString(), equalTo("t"));
    assertThat("Statement literal should be identifier", actual.get(1).getLiteral().getString(), equalTo("t"));
    // No resource identifiers added to statements
  }
  
}
