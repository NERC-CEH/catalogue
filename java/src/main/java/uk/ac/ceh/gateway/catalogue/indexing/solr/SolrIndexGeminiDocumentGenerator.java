package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.util.Collections;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Processes a GeminiDocument and populates a SolrIndex object will all of the
 * bits of the document transferred. Ready to be indexed by Solr
 */
@Slf4j
@ToString
public class SolrIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, SolrIndex> {
    private static final String OGL_PATTERN1 = ".*open-government-licence.*\\/plain$";
    private static final String OGL_PATTERN2 = ".*OGL.*\\/plain$";


    private final TopicIndexer topicIndexer;
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final CodeLookupService codeLookupService;

    public SolrIndexGeminiDocumentGenerator(
            TopicIndexer topicIndexer,
            SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex,
            CodeLookupService codeLookupService
    ) {
        this.topicIndexer = topicIndexer;
        this.metadataDocumentSolrIndex = metadataDocumentSolrIndex;
        this.codeLookupService = codeLookupService;
        log.info("Creating");
    }

    @Override
    public SolrIndex generateIndex(GeminiDocument document) {
        return metadataDocumentSolrIndex
            .generateIndex(document)
            .setAltTitle(document.getAlternateTitles())
            .setAuthorAffiliation(grab(document.getAuthors(), ResponsibleParty::getOrganisationName))
            .setAuthorName(grab(document.getAuthors(), ResponsibleParty::getIndividualName))
            .setAuthorOrcid(grab(document.getAuthors(), ResponsibleParty::getNameIdentifier))
            .setAuthorRor(grab(document.getAuthors(), ResponsibleParty::getOrganisationIdentifier))
            .setFunder(grab(document.getFunding(), Funding::getFunderName))
            .setGrant(grab(document.getFunding(), Funding::getAwardNumber))
            .setIncomingCitationCount(document.getIncomingCitationCount())
            .setIndividual(grab(document.getResponsibleParties(), ResponsibleParty::getIndividualName))
            .setLicence(getLicence(document))
            .setLineage(document.getLineage())
            .setOrcid(grab(document.getResponsibleParties(), ResponsibleParty::getNameIdentifier))
            .setOrganisation(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
            .setResourceIdentifier(grab(document.getResourceIdentifiers(), ResourceIdentifier::getCode))
            .setResourceStatus(document.getResourceStatus())
            .setRor(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationIdentifier))
            .setRightsHolder(grab(document.getRightsHolders(), ResponsibleParty::getOrganisationName))
            .setSupplementalDescription(grab(document.getSupplemental(), Supplemental::getDescription))
            .setSupplementalName(grab(document.getSupplemental(), Supplemental::getName))
            .setTopic(topicIndexer.index(document))
            .setVersion(document.getVersion())
            ;
    }

    private String getLicence(GeminiDocument document){
        return codeLookupService.lookup("licence.isOgl", hasOglLicence(document));
    }

    private boolean hasOglLicence(GeminiDocument document) {
        return Optional.ofNullable(document.getUseConstraints())
            .orElse(Collections.emptyList())
            .stream()
            .filter(k -> !k.getUri().isEmpty())
            .anyMatch(k -> {
                String uri = k.getUri();
                return uri.matches(OGL_PATTERN1) || uri.matches(OGL_PATTERN2);
            });
    }

}
