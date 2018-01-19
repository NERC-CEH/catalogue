/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ceh.gateway.catalogue.indexing;

import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class JenaIndexGeminiDocumentGeneratorTest {
  @Mock private DocumentIdentifierService service;
  private JenaIndexGeminiDocumentGenerator generator;
  
  @Before
  public void setup() {
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
