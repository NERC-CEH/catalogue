package uk.ac.ceh.gateway.catalogue.indexing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SolrIndexOsdpMonitoringActivityGeneratorTest {
    @Mock SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;
    @Mock SolrGeometryService geometryService;
    private SolrIndexOsdpMonitoringActivityGenerator generator;

    @Before
    public void setup() {
        generator = new SolrIndexOsdpMonitoringActivityGenerator(
            metadataDocumentGenerator,
            geometryService
        );
    }

    @Test
    public void checkThatBoundingBoxAdded() {
        //Given
        String id = "c3a62369-4556-4820-8e6b-7b6c962175ea";
        MonitoringActivity document = new MonitoringActivity();
        document.setId(id);
        document.setBoundingBox(
            BoundingBox.builder()
                .northBoundLatitude("56")
                .eastBoundLongitude("3")
                .southBoundLatitude("54")
                .westBoundLongitude("-1")
                .build()
        );

        given(metadataDocumentGenerator.generateIndex(document)).willReturn(new SolrIndex());
        given(geometryService.toSolrGeometry(any(String.class))).willReturn("WKT");

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        verify(geometryService).toSolrGeometry(any(String.class));
        assertThat("Should be one bounding box", index.getLocations().size(), equalTo(1));
    }

    @Test
    public void createSolrIndexWithNoGeometryOrBoundingBox() {
        //Given
        String id = "c3a62369-4556-4820-8e6b-7b6c962175ea";
        MonitoringActivity document = new MonitoringActivity();
        document.setId(id);

        given(metadataDocumentGenerator.generateIndex(document)).willReturn(new SolrIndex());

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat("Should be no bounding box", index.getLocations().isEmpty(), is(true));
    }

}
