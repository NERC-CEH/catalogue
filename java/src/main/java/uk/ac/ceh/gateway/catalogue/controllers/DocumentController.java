package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Controller
public class DocumentController extends AbstractDocumentController {
    public static final String MAINTENANCE_ROLE = "ROLE_CIG_SYSTEM_ADMIN";
    
    @Autowired
    public DocumentController(DocumentRepository documentRepository) {
        super(documentRepository);
    }
    
    @RequestMapping (value = "id/{id}",
                     method = RequestMethod.GET)
    public RedirectView redirectToResource(
            @PathVariable("id") String id,
            HttpServletRequest request
    ) {
        UriComponentsBuilder url = ServletUriComponentsBuilder
                                            .fromRequest(request)
                                            .scheme("https")
                                            .replacePath("documents/{id}");
        RedirectView toReturn = new RedirectView(url.buildAndExpand(id).toUriString());
        toReturn.setStatusCode(HttpStatus.SEE_OTHER);
        return toReturn;
    }
    
    @RequestMapping (value = "documents/upload",
                     method = RequestMethod.GET)
    public ModelAndView uploadForm() {
        return new ModelAndView("/html/upload.html.tpl");
    }
    
    @PreAuthorize("@permission.userCanCreate(#catalogue)")
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
    
    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newModelDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody ImpDocument document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new Model Document"
        );
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping (value = "documents/{file}",
                     method = RequestMethod.PUT,
                     consumes = MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateModelDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody ImpDocument document
    ) throws DocumentRepositoryException  {
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }
    
    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newGeminiDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody GeminiDocument document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new Gemini Document"
        );
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
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }
    
    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = CEH_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newCehModelDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody CehModel document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new CEH Model document"
        );
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.PUT,
                    consumes = CEH_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateCehModelDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody CehModel document
    ) throws DocumentRepositoryException  {      
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }
    
    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = CEH_MODEL_APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newCehModelApplicationDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody CehModelApplication document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new CEH Model document"
        );
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.PUT,
                    consumes = CEH_MODEL_APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateCehModelApplicationDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody CehModelApplication document
    ) throws DocumentRepositoryException  {      
        return saveMetadataDocument(
            user,
            file,
            document
        );
    }
    
    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST,
                     consumes = LINKED_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newLinkDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody LinkDocument document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException  {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new Linked Document"
        );
    }
        
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
                    method = RequestMethod.PUT,
                    consumes = LINKED_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateLinkDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody LinkDocument document
    ) throws DocumentRepositoryException  {    
        return saveMetadataDocument(
            user,
            file,
            document
        );
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
            LinkDocument linkDocument = (LinkDocument) document;
            String id = linkDocument.getId();
            String uri = linkDocument.getUri();
            List<Keyword> additionalKeywords = linkDocument.getAdditionalKeywords();
            MetadataInfo metadataInfo = linkDocument.getMetadata();
            MetadataInfo masterMetadataInfo = linkDocument.getOriginal().getMetadata();
            if (masterMetadataInfo.isPubliclyViewable(Permission.VIEW)) {
                document = linkDocument.getOriginal();
                document.setMetadata(metadataInfo);
                document.setId(id);
                document.setUri(uri);
                document.addAdditionalKeywords(additionalKeywords);
            }
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
