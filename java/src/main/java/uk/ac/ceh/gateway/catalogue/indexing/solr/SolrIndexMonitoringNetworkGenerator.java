package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;

import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Processes a MonitoringNetwork and populates a SolrIndex object with all of the
 * bits of the document transferred. Ready to be indexed by Solr
 */
@Slf4j
@ToString
public class SolrIndexMonitoringNetworkGenerator implements IndexGenerator<MonitoringNetwork, SolrIndex> {

    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;

    public SolrIndexMonitoringNetworkGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex) {
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        log.info("Creating {}", this);
    }

    @Override
    public SolrIndex generateIndex(MonitoringNetwork document) {
        return metadataDocumentSolrIndex
            .generateIndex(document)
            .setObjectives(document.getObjectives())
            .setEnvironmentalDomains(grab(document.getEnvironmentalDomain(), Keyword::getValue))
            .setKeywordsParameters(grab(document.getKeywordsParameters(), Keyword::getValue))
            .setResponsibleParties(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
            ;
    }
}
