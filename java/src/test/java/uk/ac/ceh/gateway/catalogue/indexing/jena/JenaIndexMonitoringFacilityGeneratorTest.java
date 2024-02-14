package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.val;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.EIDCUSES;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.IDENTIFIER;

@ExtendWith(MockitoExtension.class)
public class JenaIndexMonitoringFacilityGeneratorTest {
    @InjectMocks private JenaIndexMonitoringFacilityGenerator service;
    @Mock private JenaIndexMetadataDocumentGenerator generator;
    private String baseUri = "baseUri";
    private MonitoringFacility document;
    @BeforeEach
    void setUp() {
        document = new MonitoringFacility();
        Resource monitoringFacilityResource = ResourceFactory.createResource("http://monitoringFacility");
        when(generator.resource(document.getId())).thenReturn(monitoringFacilityResource);
        service = new JenaIndexMonitoringFacilityGenerator(generator, baseUri);
    }
    @Test
    void geometryShouldBeIndexedWhenProvided() {
        //Given
        String exampleGeometry = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-2.76856,58.56252]}}";
        document.setGeometry(Geometry.builder().geometryString(exampleGeometry).build());

        //When
        List<Statement> actual = service.generateIndex(document);

        //Then
        assertThat("Should be one identifier statements", actual.size(), equalTo(1));
        assertThat("Geometry statement should be as expected", actual.get(0).getLiteral().getString(), equalTo(exampleGeometry));
    }


    @Test
    void relatedRecordsShouldBeIndexedWhenProvided() {
        //Given
        String relatedUrl = "https://example.com/related";
        String relatedId = "1";
        String documentUrl = "https://example.com/document/1";
        String title = "title";
        String description = "something";
        RelatedRecord relatedRecord = new RelatedRecord(relatedUrl, relatedId, documentUrl, title, description);
        document.setRelatedRecords(Collections.singletonList(relatedRecord));

        //When
        List<Statement> actual = service.generateIndex(document);

        //Then
        assertThat("Should be one identifier statements", actual.size(), equalTo(1));
        assertThat("relatedRecords statement should be as expected", actual.get(0).getPredicate().toString(), equalTo(relatedUrl));
    }

    @Test
    void geometryShouldNotBeIndexedWhenNotProvided() {
        //Given

        //When
        List<Statement> actual = service.generateIndex(document);

        //Then
        assertThat("Should be no statements if no indexed fields present", actual.size(), equalTo(0));
    }
}
