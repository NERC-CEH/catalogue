package uk.ac.ceh.gateway.catalogue.indexing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SolrIndexOsdpMonitoringFacilityGeneratorTest {
    @Mock SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;
    @Mock SolrGeometryService geometryService;
    private SolrIndexOsdpMonitoringFacilityGenerator generator;

    @Before
    public void setup() {
        generator = new SolrIndexOsdpMonitoringFacilityGenerator(
            metadataDocumentGenerator,
            geometryService
        );
    }

    @Test
    public void checkThatBoundingBoxAddedForGeometry() {
        //Given
        String id = "c3a62369-4556-4820-8e6b-7b6c962175ea";
        MonitoringFacility document = new MonitoringFacility();
        document.setId(id);
        document.setBoundingBox(
            BoundingBox.builder()
                .northBoundLatitude("56")
                .eastBoundLongitude("3")
                .southBoundLatitude("54")
                .westBoundLongitude("-1")
                .build()
        );
        document.setGeometry("POLYGON");

        given(metadataDocumentGenerator.generateIndex(document)).willReturn(new SolrIndex());
        given(geometryService.toSolrGeometry(any(String.class))).willReturn("WKT");

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        verify(geometryService, times(2)).toSolrGeometry(any(String.class));
        assertThat("Should be two locations", index.getLocations().size(), equalTo(2));
    }

    @Test
    public void createSolrIndexWithNoGeometryOrBoundingBox() {
        //Given
        String id = "c3a62369-4556-4820-8e6b-7b6c962175ea";
        MonitoringFacility document = new MonitoringFacility();
        document.setId(id);

        given(metadataDocumentGenerator.generateIndex(document)).willReturn(new SolrIndex());

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat("Should be no bounding box", index.getLocations().isEmpty(), is(true));
    }

}
