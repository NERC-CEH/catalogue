package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.CitationService;

@Controller
@AllArgsConstructor
public class CitationController {
    private final DocumentRepository documentRepository;
    private final CitationService citationService;
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value  = "documents/{file}/citation",
                    method = RequestMethod.GET)
    @ResponseBody
    public Citation getCitation(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file
    ) throws DocumentRepositoryException {          
        return getCitation(documentRepository.read(file));
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @RequestMapping(value  = "history/{revision}/{file}/citation",
                    method = RequestMethod.GET)
    @ResponseBody
    public Citation getCitation(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @PathVariable("revision") String revision
    ) throws DocumentRepositoryException  {
        return getCitation(documentRepository.read(file, revision));
    }
    
    protected Citation getCitation(MetadataDocument document) {
        if(document instanceof GeminiDocument) {
            GeminiDocument gemini = (GeminiDocument)document;
            return citationService.getCitation(gemini)
                .orElseThrow(
                    () -> new ResourceNotFoundException("The Document is not citable")
                );
        }
        else {
            throw new ResourceNotFoundException("Only Gemini Documents can have citations");
        }
    }
}
