package uk.ac.ceh.gateway.catalogue.indexing;

import org.springframework.beans.factory.annotation.Autowired;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;

import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;


/**
 * Based upon a general MetadataDocument generator, instances of this class will
 * index SampleArchive specific details.
 */
public class SolrIndexSaMonitoringFacilityGenerator implements IndexGenerator<SampleArchive, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;

    @Autowired
    public SolrIndexSaMonitoringFacilityGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex, SolrGeometryService geometryService) {
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        this.geometryService = geometryService;
    }

    @Override
    public SolrIndex generateIndex(SampleArchive sa) {
      return metadataDocumentSolrIndex
          .generateIndex(sa)
          .addLocations(geometryService.toSolrGeometry(grab(sa.getBoundingBoxes(), BoundingBox::getWkt)));
        }    

          
}
