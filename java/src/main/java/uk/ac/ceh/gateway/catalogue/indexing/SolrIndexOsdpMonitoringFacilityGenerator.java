package uk.ac.ceh.gateway.catalogue.indexing;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

import java.util.Arrays;

/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index OsdpMonitoringFacility specific details.
 */
public class SolrIndexOsdpMonitoringFacilityGenerator implements IndexGenerator<MonitoringFacility, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;

    @Autowired
    public SolrIndexOsdpMonitoringFacilityGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex, SolrGeometryService geometryService) {
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        this.geometryService = geometryService;
    }

    @Override
    public SolrIndex generateIndex(MonitoringFacility mf) {
        return metadataDocumentSolrIndex
            .generateIndex(mf)
            .addLocations(
                Arrays.asList(
                    geometryService.toSolrGeometry(mf.getBoundingBox().getWkt()),
                    geometryService.toSolrGeometry(mf.getGeometry())
                )
            );
    }    
}
