package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.osdp.Dataset;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index OsdpDataset specific details.
 */
@AllArgsConstructor
public class SolrIndexOsdpDatasetGenerator implements IndexGenerator<Dataset, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;

    @Override
    public SolrIndex generateIndex(Dataset d) {
        SolrIndex toReturn = metadataDocumentSolrIndex.generateIndex(d);
        if (d.getBoundingBox() != null) {
            toReturn.addLocation(geometryService.toSolrGeometry(d.getBoundingBox().getWkt()));
        }
        return toReturn;
    }
}
