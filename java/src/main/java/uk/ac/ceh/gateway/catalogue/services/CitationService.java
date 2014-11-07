package uk.ac.ceh.gateway.catalogue.services;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.ResponsibleParty;

/**
 *
 * @author cjohn
 */
public class CitationService {
    public static final String DOI_CODE_SPACE = "doi:";
    public static final String NERC_DOI_PREFIX = "10.5285/";
    
    /**
     * Generate a citation value for the current geminidocument. If the document
     * is not citable, just return null
     * @param geminiDocument The geminidocument to cite
     * @return Either a valid citation object or null
     */
    public Citation getCitation(GeminiDocument geminiDocument) {
        Optional<ResourceIdentifier> citationResource = geminiDocument
                .getResourceIdentifiers()
                .stream()
                .filter((r)->r.getCodeSpace().equals(DOI_CODE_SPACE))
                .filter((r)->r.getCode().startsWith(NERC_DOI_PREFIX))
                .findFirst();
        
        //If is present and rest of document is valid
        if(citationResource.isPresent()) {
            ResourceIdentifier doi = citationResource.get();
            
            DatasetReferenceDate referenceDate = geminiDocument.getDatasetReferenceDate();
            LocalDate pubDate = (referenceDate != null) ? referenceDate.getPublicationDate() : null;
            
            Optional<ResponsibleParty> publisher = getPublisher(geminiDocument);
            
            if (pubDate!= null && publisher.isPresent()) {
                return Citation
                        .builder()
                        .authors(   getAuthors(geminiDocument))
                        .doi(       doi.getCode())
                        .title(     geminiDocument.getTitle())
                        .year(      pubDate.getYear())
                        .publisher( publisher.get().getOrganisationName())
                        .bibtex(    getInAlternateFormat(geminiDocument, WebConfig.BIBTEX_SHORT))
                        .ris(       getInAlternateFormat(geminiDocument, WebConfig.RESEARCH_INFO_SYSTEMS_SHORT))
                        .build();
            }
        }
        return null;        
    }
    
    protected URI getInAlternateFormat(GeminiDocument geminiDocument, String alternateFormat) {
        return UriComponentsBuilder.fromUri(geminiDocument.getUri())
                                   .path("/citation")
                                   .queryParam("format", alternateFormat)
                                   .build()
                                   .toUri();
    }
    
    protected Optional<ResponsibleParty> getPublisher(GeminiDocument geminiDocument) {
        return geminiDocument
                .getResponsibleParties()
                .stream()
                .filter((p) -> "Publisher".equals(p.getRole()))
                .findFirst();
    }
    
    protected List<String> getAuthors(GeminiDocument geminiDocument) {
        return geminiDocument.getResponsibleParties()
                             .stream()
                             .filter((p) -> p.getRole().equals("Author"))
                             .map(ResponsibleParty::getIndividualName)
                             .collect(Collectors.toList());
    }
}
