package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index OsdpMonitoringFacility specific details.
 */
@AllArgsConstructor
public class SolrIndexOsdpMonitoringFacilityGenerator implements IndexGenerator<MonitoringFacility, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;

    @Override
    public SolrIndex generateIndex(MonitoringFacility mf) {
        return metadataDocumentSolrIndex
            .generateIndex(mf)
            .addLocation(geometryService.toSolrGeometry(mf.getGeometry()))
            .addLocations(geometryService.toSolrGeometry(grab(mf.getBoundingBox(), BoundingBox::getWkt)));
    }    
}
