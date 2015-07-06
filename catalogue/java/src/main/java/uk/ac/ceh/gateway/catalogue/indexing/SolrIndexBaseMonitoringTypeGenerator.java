package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.Data;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 *
 * @author cjohn
 */
@Data
public class SolrIndexBaseMonitoringTypeGenerator implements IndexGenerator<BaseMonitoringType, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;
    
    @Override
    public SolrIndex generateIndex(BaseMonitoringType ef) {
        return metadataDocumentSolrIndex
                .generateIndex(ef)
                .addLocations(geometryService.toSolrGeometry(grab(ef.getBoundingBoxes(), BaseMonitoringType.BoundingBox::getWkt)));
    }    
}
