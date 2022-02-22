package uk.ac.ceh.gateway.catalogue.indexing.solr;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.ri.RiDatacube;


import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

public class RiDatacubeIndexGenerator implements IndexGenerator<RiDatacube, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;

    public RiDatacubeIndexGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentGenerator) {
        this.metadataDocumentGenerator = metadataDocumentGenerator;
    }

    @Override
    public SolrIndex generateIndex(RiDatacube document) throws DocumentIndexingException {
        return metadataDocumentGenerator.generateIndex(document)
            .setInfrastructureCategory(document.getInfrastructureCategory())
            .setScienceArea(document.getScienceArea())
            .setInfrastructureCapabilities(document.getCapabilities())
            .setInfrastructureScale(document.getInfrastructureScale())
            .setInfrastructureChallenge(grab(document.getInfrastructureChallenge(), Keyword::getValue))
;
    }
}
