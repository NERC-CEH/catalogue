package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_JSON_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.LINKED_JSON_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.MODEL_JSON_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Controller
public class DocumentController {
    public static final String EDITOR_ROLE = "ROLE_CIG_EDITOR";
    public static final String PUBLISHER_ROLE = "ROLE_CIG_PUBLISHER";
    public static final String MAINTENANCE_ROLE = "ROLE_CIG_SYSTEM_ADMIN";
    private final DocumentRepository documentRepository;
    
    @Autowired
    public DocumentController(
        DocumentRepository documentRepository
    ) {
        this.documentRepository = documentRepository;
    }
    
    @RequestMapping (value = "id/{id}",
                     method = RequestMethod.GET)
    public RedirectView redirectToResource(
            @PathVariable("id") String id,
            HttpServletRequest request
    ) {
        UriComponentsBuilder url = ServletUriComponentsBuilder
                                            .fromRequest(request)
                                            .replacePath("documents/{id}");
        RedirectView toReturn = new RedirectView(url.buildAndExpand(id).toUriString());
        toReturn.setStatusCode(HttpStatus.SEE_OTHER);
        return toReturn;
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
    public Object uploadFile(
            @ActiveUser CatalogueUser user,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("type") String documentType,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException, IOException  {
        
        MetadataDocument data = documentRepository.save(
            user,
            multipartFile.getInputStream(),
            MediaType.parseMediaType(multipartFile.getContentType()),
            documentType,
            catalogue,
            "new file upload"
        );
        return new RedirectView(data.getUri());
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> uploadModelDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody Model document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
       
        MetadataDocument data = documentRepository.saveNew(
            user,
            document,
            catalogue,
            "new Model Document"
        );
        return ResponseEntity
            .created(URI.create(data.getUri()))
            .body(data);
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping (value = "documents/{file}",
                     method = RequestMethod.PUT,
                     consumes = MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateModelDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody Model document
    ) throws DocumentRepositoryException  {
       
        MetadataDocument data = documentRepository.save(
            user,
            document,
            file,
            String.format("Edited document: %s", file)
        );
        return ResponseEntity
            .ok(data);
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.PUT,
                    consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateGeminiDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody GeminiDocument document
    ) throws DocumentRepositoryException  {
              
        MetadataDocument data = documentRepository.save(
            user,
            document,
            file,
            String.format("Edited document: %s", file)
        );
        return ResponseEntity
            .ok(data);
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> uploadGeminiDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody GeminiDocument document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
       
        MetadataDocument data = documentRepository.saveNew(
            user,
            document,
            catalogue,
            "new Gemini Document"
        );
        return ResponseEntity
            .created(URI.create(data.getUri()))
            .body(data);
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.PUT,
                    consumes = LINKED_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateLinkedDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody LinkDocument document
    ) throws DocumentRepositoryException  {
              
        MetadataDocument data = documentRepository.save(
            user,
            document,
            file,
            String.format("Edited document: %s", file)
        );
        return ResponseEntity
            .ok(data);
    }
    
    @Secured(EDITOR_ROLE)
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = LINKED_JSON_VALUE)
    public ResponseEntity<MetadataDocument> uploadLinkedDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody LinkDocument document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
       
        MetadataDocument data = documentRepository.saveNew(
            user,
            document,
            catalogue,
            "new Linked Document"
        );
        return ResponseEntity
            .created(URI.create(data.getUri()))
            .body(data);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.GET)   
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file
    ) throws DocumentRepositoryException  {        
        return postprocessLinkDocument(documentRepository.read(file));
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.GET,
                    produces = LINKED_JSON_VALUE)   
    @ResponseBody
    public MetadataDocument readLinkDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file
    ) throws DocumentRepositoryException  {
        return documentRepository.read(file);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @RequestMapping(value = "history/{revision}/{file}",
                    method = RequestMethod.GET)
    @ResponseBody
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("revision") String revision
    ) throws DocumentRepositoryException {
        return postprocessLinkDocument(documentRepository.read(file, revision));
    }
    
    private MetadataDocument postprocessLinkDocument(MetadataDocument document) {
        if (document instanceof LinkDocument) {
            String id = document.getId();
            String uri = document.getUri();
            MetadataInfo metadataInfo = document.getMetadata();
            document = ((LinkDocument) document).getOriginal();
            document.setMetadata(metadataInfo);
            document.setId(id);
            document.setUri(uri);
        }
        return document;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'DELETE')")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.DELETE)
    @ResponseBody
    public DataRevision<CatalogueUser> deleteDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file
    ) throws DocumentRepositoryException {    
        return documentRepository.delete(user, file);
    }
}
