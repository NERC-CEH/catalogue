package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.ef.Facility;
import uk.ac.ceh.gateway.catalogue.ef.Geometry;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 *
 * @author cjohn
 */
@Data
public class SolrIndexFacilityGenerator implements IndexGenerator<Facility, SolrIndex> {
    private final SolrIndexBaseMonitoringTypeGenerator metadataDocumentSolrIndex;
    
    private final SolrGeometryService geometryService;
    
    @Override
    public SolrIndex generateIndex(Facility ef) {     
        return metadataDocumentSolrIndex
                .generateIndex(ef)
                .addLocations(geometryService.toSolrGeometry(grab(Arrays.asList(ef.getGeometry()), Geometry::getValue)));
    }    
}
