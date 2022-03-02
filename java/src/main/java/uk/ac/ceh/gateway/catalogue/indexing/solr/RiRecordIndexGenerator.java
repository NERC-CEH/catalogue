package uk.ac.ceh.gateway.catalogue.indexing.solr;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.ri.RiRecord;


import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

public class RiRecordIndexGenerator implements IndexGenerator<RiRecord, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;

    public RiRecordIndexGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentGenerator) {
        this.metadataDocumentGenerator = metadataDocumentGenerator;
    }

    @Override
    public SolrIndex generateIndex(RiRecord document) throws DocumentIndexingException {
        return metadataDocumentGenerator.generateIndex(document)
            .setInfrastructureCategory(document.getInfrastructureCategory())
            .setScienceArea(document.getScienceArea())
            .setInfrastructureCapabilities(document.getCapabilities())
            .setInfrastructureScale(document.getInfrastructureScale())
            .setInfrastructureChallenge(grab(document.getInfrastructureChallenge(), Keyword::getValue))
;
    }
}
