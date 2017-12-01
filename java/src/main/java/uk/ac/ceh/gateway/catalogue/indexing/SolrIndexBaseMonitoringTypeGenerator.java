package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.ef.BaseMonitoringType;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index BaseMonitoringType specific details.
 */
@AllArgsConstructor
public class SolrIndexBaseMonitoringTypeGenerator implements IndexGenerator<BaseMonitoringType, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator solrIndexMetadataDocumentGenerator;
    private final SolrGeometryService solrGeometryService;
    
    @Override
    public SolrIndex generateIndex(BaseMonitoringType ef) {
        return solrIndexMetadataDocumentGenerator
                .generateIndex(ef)
                .addLocations(solrGeometryService.toSolrGeometry(grab(ef.getBoundingBoxes(), BaseMonitoringType.BoundingBox::getWkt)));
    }    
}
