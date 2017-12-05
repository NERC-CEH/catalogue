package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.osdp.Sample;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index OsdpSample specific details.
 */
@AllArgsConstructor
public class SolrIndexOsdpSampleGenerator implements IndexGenerator<Sample, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;

    @Override
    public SolrIndex generateIndex(Sample s) {
        SolrIndex toReturn = metadataDocumentSolrIndex.generateIndex(s);
        if (s.getGeometry() != null) {
            toReturn.addLocation(geometryService.toSolrGeometry(s.getGeometry()));
        }
        return toReturn;
    }
}
