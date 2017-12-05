package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.osdp.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index OsdpMonitoringProgramme specific details.
 */
@AllArgsConstructor
public class SolrIndexOsdpMonitoringProgrammeGenerator implements IndexGenerator<MonitoringProgramme, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;

    @Override
    public SolrIndex generateIndex(MonitoringProgramme mp) {
        SolrIndex toReturn = metadataDocumentSolrIndex.generateIndex(mp);
        if (mp.getBoundingBox() != null) {
            toReturn.addLocation(geometryService.toSolrGeometry(mp.getBoundingBox().getWkt()));
        }
        return toReturn;
    }
}
