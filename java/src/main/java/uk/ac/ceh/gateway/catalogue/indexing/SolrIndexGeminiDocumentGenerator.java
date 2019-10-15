package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.Supplemental;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;

import java.util.Collections;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Processes a GeminiDocument and populates a SolrIndex object will all of the
 * bits of the document transferred. Ready to be indexed by Solr
 */
@AllArgsConstructor
public class SolrIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, SolrIndex> {
    private static final String OGL_PATTERN1 = ".*open-government-licence.*\\/plain$";
    private static final String OGL_PATTERN2 = ".*OGL.*\\/plain$";


    private final TopicIndexer topicIndexer;
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final CodeLookupService codeLookupService;
    
    @Override
    public SolrIndex generateIndex(GeminiDocument document) {
        return metadataDocumentSolrIndex
            .generateIndex(document)
            .setTopic(topicIndexer.index(document))
            .setAltTitle(document.getAlternateTitles())
            .setLineage(document.getLineage())
            .setResourceStatus(document.getResourceStatus())
            .setVersion(document.getVersion())
            .setLicence(getLicence(document))
            .setOrganisation(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
            .setIndividual(grab(document.getResponsibleParties(), ResponsibleParty::getIndividualName))
            .setOrcid(grab(document.getResponsibleParties(), ResponsibleParty::getNameIdentifier))
            .setRor(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationIdentifier))
            .setResourceIdentifier(grab(document.getResourceIdentifiers(), ResourceIdentifier::getCode))
            .setGrant(grab(document.getFunding(), Funding::getAwardNumber))
            .setFunder(grab(document.getFunding(), Funding::getFunderName))
            .setSupplementalName(grab(document.getSupplemental(), Supplemental::getName))
            .setSupplementalDescription(grab(document.getSupplemental(), Supplemental::getDescription))
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
