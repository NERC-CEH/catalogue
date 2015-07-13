package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.ResponsibleParty;
import static uk.ac.ceh.gateway.catalogue.indexing.SolrIndexMetadataDocumentGenerator.grab;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 * Processes a GeminiDocument and populates a SolrIndex object will all of the
 * bits of the document transferred. Ready to be indexed by Solr
 * @author cjohn
 */
@Data
public class SolrIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, SolrIndex> {
    private static final String OGL_URL = "http://www.nationalarchives.gov.uk/doc/open-government-licence";
    private static final String CEH_OGL_URL = "http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence";
    
    private final SolrIndexMetadataDocumentGenerator metadataDocumentSolrIndex;
    private final SolrGeometryService geometryService;
    private final CodeLookupService codeLookupService;
    
    @Override
    public SolrIndex generateIndex(GeminiDocument document) {
        return metadataDocumentSolrIndex
                .generateIndex(document)
                .setAltTitle(document.getAlternateTitles())
                .setLineage(document.getLineage())
                .setLicence(getLicence(document))
                .setOrganisation(grab(document.getResponsibleParties(), ResponsibleParty::getOrganisationName))
                .setIndividual(grab(document.getResponsibleParties(), ResponsibleParty::getIndividualName))
                .setOnlineResourceName(grab(document.getOnlineResources(), OnlineResource::getName))
                .setOnlineResourceDescription(grab(document.getOnlineResources(), OnlineResource::getDescription))
                .setResourceIdentifier(grab(document.getResourceIdentifiers(), ResourceIdentifier::getCode))
                .setKeyword(grab(getKeywords(document), Keyword::getValue))
                .setDataCentre(getDataCentre(document))
                .addLocations(geometryService.toSolrGeometry(grab(document.getBoundingBoxes(), BoundingBox::getWkt)));
    }

    private List<Keyword> getKeywords(GeminiDocument document) {
        return Optional.ofNullable(document.getDescriptiveKeywords())
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(d -> d.getKeywords().stream())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private String getLicence(GeminiDocument document){
        boolean ogl = false;
        List<Keyword> licences = Optional.ofNullable(document.getUseLimitations())
            .orElse(Collections.emptyList());

        for (Keyword keyword : licences) {
            if ( !keyword.getUri().isEmpty()) {
                String uri = keyword.getUri();
                if (uri.startsWith(CEH_OGL_URL) || uri.startsWith(OGL_URL)) {
                    ogl = true;
                    break;
                }
            }
        }
        return codeLookupService.lookup("licence.isOgl", ogl);
    }
        
    private String getDataCentre(GeminiDocument document) {
        Optional<ResponsibleParty> dataCentre = document.getResponsibleParties()
            .stream()
            .filter(rp -> rp.getRole().equals("custodian") && rp.getOrganisationName().startsWith("EIDC"))
            .findFirst();
        
        if (dataCentre.isPresent()) {
            return "EIDCHub";
        } else {
            return "";
        }
    }
}
