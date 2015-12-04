package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_JSON_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

@Controller
public class DocumentController {
    public static final String EDITOR_ROLE = "ROLE_CIG_EDITOR";
    public static final String PUBLISHER_ROLE = "ROLE_CIG_PUBLISHER";
    public static final String MAINTENANCE_ROLE = "ROLE_CIG_SYSTEM_ADMIN";
    private final DocumentRepository documentRepository;
    
    @Autowired
    public DocumentController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents/upload",
                     method = RequestMethod.GET)
    public ModelAndView uploadForm() {
        return new ModelAndView("/html/upload.html.tpl");
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Object uploadDocument(
            @ActiveUser CatalogueUser user,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("type") String documentType) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException  {
        
        MetadataDocument data = documentRepository.save(
            user,
            multipartFile.getInputStream(),
            MediaType.parseMediaType(multipartFile.getContentType()),
            documentType
        );
        return new RedirectView(data.getUri().toString());
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> uploadDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody GeminiDocument geminiDocument) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException  {
       
        MetadataDocument data = documentRepository.save(user, geminiDocument);
        return ResponseEntity
            .created(data.getUri())
            .body(data);
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.PUT,
                    consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody GeminiDocument geminiDocument) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException  {
              
        MetadataDocument data = documentRepository.save(user, geminiDocument, file);
        return ResponseEntity
            .ok(data);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.GET)   
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException  {
        
        return documentRepository.read(file);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @RequestMapping(value = "history/{revision}/{file}",
                    method = RequestMethod.GET)
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("revision") String revision) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        
        return documentRepository.read(file, revision);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'DELETE')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.DELETE)
    @ResponseBody
    public DataRevision<CatalogueUser> deleteDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) throws DataRepositoryException, IOException {
        
        return documentRepository.delete(user, file);
    }
}
