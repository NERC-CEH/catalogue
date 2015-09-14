package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.lake.LakeDocument;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 *
 * @author cjohn
 */
@Data
public class SolrIndexLakeDocumentGenerator implements IndexGenerator<LakeDocument, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;
    
    @Override
    public SolrIndex generateIndex(LakeDocument toIndex) throws DocumentIndexingException {
        return metadataDocumentSolrIndex
                .generateIndex(toIndex)
                .addLocations(geometryService.toSolrGeometry(Arrays.asList(toIndex.getGeometry())));
    }
    
}
