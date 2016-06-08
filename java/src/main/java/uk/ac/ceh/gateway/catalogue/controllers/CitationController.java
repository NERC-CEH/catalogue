package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 *
 * @author cjohn
 */
@Controller
public class CitationController {
    private final DocumentRepository documentRepository;
    
    @Autowired
    public CitationController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value  = "documents/{file}/citation",
                    method = RequestMethod.GET)
    @ResponseBody
    public Citation getCitation(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
                
        return getCitation(documentRepository.read(file));
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @RequestMapping(value  = "history/{revision}/{file}/citation",
                    method = RequestMethod.GET)
    @ResponseBody
    public Citation getCitation(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @PathVariable("revision") String revision) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException  {
        
        return getCitation(documentRepository.read(file, revision));
    }
    
    protected Citation getCitation(MetadataDocument document) {
        if(document instanceof GeminiDocument) {
            GeminiDocument gemini = (GeminiDocument)document;
            Citation citation = gemini.getCitation();
            if(citation != null) {
                return citation;
            }
            else {
                throw new ResourceNotFoundException("The Document is not citable");
            }
        }
        else {
            throw new ResourceNotFoundException("Only Gemini Documents can have citations");
        }
    }
}
