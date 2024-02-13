package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.val;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class JenaIndexMonitoringFacilityGeneratorTest {
    private final String baseURI;

    private JenaIndexMonitoringFacilityGenerator generator;
    @Mock private DocumentIdentifierService service;
    public JenaIndexMonitoringFacilityGeneratorTest() {
        baseURI = "https://example.com";
    }

    @BeforeEach
    public void setup() {
        generator = new JenaIndexMonitoringFacilityGenerator(new JenaIndexMetadataDocumentGenerator(service), baseURI);
    }

    @Test
    void geometryShouldBeIndexedWhenProvided() {
        //Given
        val document = new MonitoringFacility();
        document.setId("id");
        String exampleGeometry = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-2.76856,58.56252]}}";
        document.setGeometry(Geometry.builder()
                .value(exampleGeometry)
                .build()
        );
        document.setResourceIdentifiers(List.of(ResourceIdentifier.builder().build()));
        given(service.generateUri("id")).willReturn("id");

        //When
        List<Statement> actual = generator.generateIndex(document);

        //Then
        assertThat("Should be two identifier statements", actual.size(), equalTo(2));
        // First statement is the (required) document id hence check the second for the geometry
        assertThat("Statement literal should be identifier", actual.get(1).getLiteral().getString(), equalTo(exampleGeometry));
    }

    @Test
    void geometryShouldNotBeIndexedWhenNotProvided() {
        //Given
        val document = new MonitoringFacility();
        document.setId("id");
        document.setResourceIdentifiers(List.of(ResourceIdentifier.builder().build()));
        given(service.generateUri("id")).willReturn("id");

        //When
        List<Statement> actual = generator.generateIndex(document);

        //Then
        assertThat("Should be two identifier statements", actual.size(), equalTo(1));
    }
}
