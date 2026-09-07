package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Processes a MonitoringProgramme and populates a SolrIndex object with all of the
 * bits of the document transferred. Ready to be indexed by Solr
 */
@Slf4j
@ToString
public class SolrIndexMonitoringProgrammeGenerator implements IndexGenerator<MonitoringProgramme, SolrIndex> {

    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;

    public SolrIndexMonitoringProgrammeGenerator(SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex) {
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        log.info("Creating");
    }

    @Override
    public SolrIndex generateIndex(MonitoringProgramme document) {
        return metadataDocumentSolrIndex
            .generateIndex(document)
            .setAltTitle(document.getAlternateTitles())
            .setObjectives(document.getObjectives())
            .setEnvironmentalDomains(grab(document.getEnvironmentalDomain(), Keyword::getValue))
            .setKeywordsParameters(grab(document.getKeywordsParameters(), Keyword::getValue))
            .setOrganisation(
                Stream.concat(
                    grab(document.getContacts(), ResponsibleParty::getOrganisationName).stream(),
                    grab(document.getPartners(), ResponsibleParty::getOrganisationName).stream()
                ).collect(Collectors.toList())
            )
            .setOperatingPeriod(grab(document.getOperatingPeriod(), MonitoringDocumentUtil::getTimeRange))
            .setOperationalStatus(document.getOperationalStatus() == null? "Unknown" : document.getOperationalStatus())
            ;
    }
}
