package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
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
    private static final String OGL_URL_1 = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/cehOGL";
    private static final String OGL_URL_2 = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/OGL";
    private static final String OGL_URL_3 = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence";


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
            .setOnlineResourceName(grab(document.getOnlineResources(), OnlineResource::getName))
            .setOnlineResourceDescription(grab(document.getOnlineResources(), OnlineResource::getDescription))
            .setResourceIdentifier(grab(document.getResourceIdentifiers(), ResourceIdentifier::getCode))
            .setKeyword(grab(document.getAllKeywords(), Keyword::getValue));
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
                return uri.startsWith(OGL_URL_1)
                    || uri.startsWith(OGL_URL_2)
                    || uri.startsWith(OGL_URL_3);  
            });
    }
}
