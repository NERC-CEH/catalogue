/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.EIDCUSES;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.IDENTIFIER;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JenaIndexGeminiDocumentGeneratorTest {
  private final String baseURI;
  private final String id;
  private final String uri;
  private final Statement identifierStatement;
  private final Statement uriStatement;
  private final Statement relatedStatement;

  @Mock private DocumentIdentifierService service;
  private JenaIndexGeminiDocumentGenerator generator;

  public JenaIndexGeminiDocumentGeneratorTest() {
    baseURI = "https://example.com";
    id = "02982b8d-b688-4b3a-862b-1260eeac86f4";
    uri = baseURI + "/id/" + id;
    val subject = createResource(uri);
    identifierStatement = createStatement(
        subject,
        IDENTIFIER,
        createPlainLiteral(id)
    );
    uriStatement = createStatement(
        subject,
        IDENTIFIER,
        createPlainLiteral(uri)
    );
    relatedStatement = createStatement(
        subject,
        EIDCUSES,
        createProperty("https://example.com/id/b5fbe026-d706-4ee3-8f7b-4f62e663b4b9")
    );
  }

  @BeforeEach
  public void setup() {
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
    assertThat("Should be two identifier statements", actual.size(), equalTo(2));
    assertThat("Statement literal should be identifier", actual.get(0).getLiteral().getString(), equalTo("t"));
    assertThat("Statement literal should be identifier", actual.get(1).getLiteral().getString(), equalTo("t"));
    // No resource identifiers added to statements
  }

  @Test
  void relatedRecordStatementAdded() {
    //given
    val document = new GeminiDocument();
    document.setId(id);
    document.setRelatedRecords(
        List.of(new RelatedRecord(
            EIDCUSES.getURI(),
            "b5fbe026-d706-4ee3-8f7b-4f62e663b4b9",
            "https://example.com/id/b5fbe026-d706-4ee3-8f7b-4f62e663b4b9",
            "Title",
            "Dataset",
            "Description of relationship"
        ))
    );
    given(service.generateUri(id)).willReturn(uri);

    //when
    val statements = generator.generateIndex(document);

    //when
    assertThat(statements, containsInAnyOrder(identifierStatement, uriStatement, relatedStatement));
  }

  @Test
  void missingRelatedRecordRelNoStatementAdded() {
    //given
    val document = new GeminiDocument();
    document.setId(id);
    document.setRelatedRecords(
        List.of(new RelatedRecord(
            null,
            "b5fbe026-d706-4ee3-8f7b-4f62e663b4b9",
            "https://example.com/id/b5fbe026-d706-4ee3-8f7b-4f62e663b4b9",
            "Title",
            "Dataset",
            "Description of relationship"
            ))
    );
    given(service.generateUri(id)).willReturn(uri);

    //when
    val statements = generator.generateIndex(document);

    //when
    assertThat(statements, containsInAnyOrder(identifierStatement, uriStatement));
  }

}
