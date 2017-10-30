package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

import java.util.Collections;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;

/**
 * Processes a GeminiDocument and populates a SolrIndex object will all of the
 * bits of the document transferred. Ready to be indexed by Solr
 * @author cjohn
 */
@Service
@AllArgsConstructor
public class SolrIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, SolrIndex> {
    private static final String OGL_URL = "http://www.nationalarchives.gov.uk/doc/open-government-licence";
    private static final String CEH_OGL_URL = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/cehOGL";
    private static final String OTHER_OGL_URL = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence";
    private static final String OTHER_OGL_URL_1 = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/OGLnonceh";
    
    private final TopicIndexer topicIndexer;
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;
    private final CodeLookupService codeLookupService;
    
    @Override
    public SolrIndex generateIndex(GeminiDocument document) {
        return metadataDocumentSolrIndex
            .generateIndex(document)
            .setTopic(topicIndexer.index(document))
            .setAltTitle(document.getAlternateTitles())
            .setLineage(document.getLineage())
            .setLicence(getLicence(document))
            .setOrganisation(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
            .setIndividual(grab(document.getResponsibleParties(), ResponsibleParty::getIndividualName))
            .setOnlineResourceName(grab(document.getOnlineResources(), OnlineResource::getName))
            .setOnlineResourceDescription(grab(document.getOnlineResources(), OnlineResource::getDescription))
            .setResourceIdentifier(grab(document.getResourceIdentifiers(), ResourceIdentifier::getCode))
            .setKeyword(grab(document.getAllKeywords(), Keyword::getValue))
            .addLocations(geometryService.toSolrGeometry(grab(document.getBoundingBoxes(), BoundingBox::getWkt)));
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
                return uri.startsWith(OTHER_OGL_URL)
                    || uri.startsWith(OTHER_OGL_URL_1)
                    || uri.startsWith(CEH_OGL_URL)
                    || uri.startsWith(OGL_URL);  
            });
    }
}
