/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ceh.gateway.catalogue.indexing.jena;

import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class JenaIndexGeminiDocumentGeneratorTest {
  @Mock private DocumentIdentifierService service;
  private String baseURI = "https://example.com";
  private JenaIndexGeminiDocumentGenerator generator;

  @BeforeEach
  public void setup() {
    generator = new JenaIndexGeminiDocumentGenerator(new JenaIndexMetadataDocumentGenerator(service),baseURI);
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