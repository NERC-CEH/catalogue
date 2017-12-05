package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

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
        SolrIndex toReturn = metadataDocumentSolrIndex.generateIndex(mf);
        if (mf.getGeometry() != null) {
            toReturn.addLocation(geometryService.toSolrGeometry(mf.getGeometry()));
        }
        if (mf.getBoundingBox() != null) {
            toReturn.addLocation(geometryService.toSolrGeometry(mf.getBoundingBox().getWkt()));
        }
        return toReturn;
    }
}
