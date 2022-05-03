package uk.ac.ceh.gateway.catalogue.indexing.solr;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureCategory;


import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

public class InfrastructureRecordIndexGenerator implements IndexGenerator<InfrastructureRecord, SolrIndex> {
    private final SolrIndexMetadataDocumentGenerator metadataDocumentGenerator;

    public InfrastructureRecordIndexGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentGenerator) {
        this.metadataDocumentGenerator = metadataDocumentGenerator;
    }

    @Override
    public SolrIndex generateIndex(InfrastructureRecord document) throws DocumentIndexingException {
        return metadataDocumentGenerator.generateIndex(document)
            .setScienceArea(document.getScienceArea())
            .setInfrastructureCapabilities(document.getCapabilities())
            .setInfrastructureScale(document.getInfrastructureScale())
            .setInfrastructureChallenge(grab(document.getInfrastructureChallenge(), Keyword::getValue))
            .setInfrastructureCategory(grab(document.getInfrastructureCategory(), InfrastructureCategory::getDescription))
            .setInfrastructureClass(grab(document.getInfrastructureCategory(), InfrastructureCategory::getInfrastructureClass))
;
    }
}
