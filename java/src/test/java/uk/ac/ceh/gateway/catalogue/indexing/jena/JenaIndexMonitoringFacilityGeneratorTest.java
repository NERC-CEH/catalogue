package uk.ac.ceh.gateway.catalogue.indexing.jena;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JenaIndexMonitoringFacilityGeneratorTest {
    @InjectMocks private JenaIndexMonitoringFacilityGenerator service;
    @Mock private JenaIndexMetadataDocumentGenerator generator;

    @Test
    void geometryShouldBeIndexedWhenProvided() {
        //Given
        MonitoringFacility document = mock(MonitoringFacility.class);
        String exampleGeometry = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-2.76856,58.56252]}}";
        when(document.getGeometry()).thenReturn(Geometry.builder().value(exampleGeometry).build());

        Resource monitoringFacilityResource = ResourceFactory.createResource("http://monitoringFacility");
        when(generator.resource(document.getId())).thenReturn(monitoringFacilityResource);

        //When
        List<Statement> actual = service.generateIndex(document);

        //Then
        assertThat("Should be one identifier statements", actual.size(), equalTo(1));
        assertThat("Geometry statement should be as expected", actual.get(0).getLiteral().getString(), equalTo(exampleGeometry));
    }

    @Test
    void geometryShouldNotBeIndexedWhenNotProvided() {
        //Given
        MonitoringFacility document = mock(MonitoringFacility.class);
        Resource monitoringFacilityResource = ResourceFactory.createResource("http://monitoringFacility");
        when(generator.resource(document.getId())).thenReturn(monitoringFacilityResource);

        //When
        List<Statement> actual = service.generateIndex(document);

        //Then
        assertThat("Should be no statements if Geometry not present", actual.size(), equalTo(0));
    }
}
