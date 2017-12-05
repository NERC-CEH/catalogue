package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index OsdpMonitoringActivity specific details.
 */
@AllArgsConstructor
public class SolrIndexOsdpMonitoringActivityGenerator implements IndexGenerator<MonitoringActivity, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;

    @Override
    public SolrIndex generateIndex(MonitoringActivity ma) {
        SolrIndex toReturn = metadataDocumentSolrIndex.generateIndex(ma);
        if (ma.getBoundingBox() != null) {
            toReturn.addLocation(geometryService.toSolrGeometry(ma.getBoundingBox().getWkt()));
        }
        return toReturn;
    }
}
