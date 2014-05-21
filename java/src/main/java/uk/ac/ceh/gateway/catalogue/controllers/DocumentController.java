package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.net.URI;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.User;
import uk.ac.ceh.gateway.catalogue.model.DocumentAlreadyExistsException;
import uk.ac.ceh.gateway.catalogue.gemini.Metadata;
import uk.ac.ceh.gateway.catalogue.services.JsonObjectMapperService;
import uk.ac.ceh.gateway.catalogue.services.URLLinkingService;

/**
 *
 * @author cjohn
 */
@Controller
public class DocumentController {
    
    private final DataRepository<User> repo;
    private final JsonObjectMapperService mapper;
    private final URLLinkingService links;
    
    @Autowired
    public DocumentController(  DataRepository<User> repo,
                                URLLinkingService links,
                                JsonObjectMapperService mapper) {
        this.repo = repo;
        this.links = links;
        this.mapper = mapper;
    }
    
    @Secured("ROLE_USER")
    @RequestMapping (value = "documents",
                     method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Metadata> uploadDocument(
            @ActiveUser User user,
            @RequestParam(value = "message", defaultValue = "new document") String commitMessage,
            @Valid @RequestBody Metadata document) throws DataRepositoryException {
        
        if(!repo.getFiles().contains(document.getId())) {
            repo.submitData(document.getId(), mapper.of(document))
                .commit(user, commitMessage);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(links.getEndpointForDocument(document.getId())));
            
            return new ResponseEntity<>(document, headers, HttpStatus.CREATED);
        } else {
            throw new DocumentAlreadyExistsException("A document already exists with id " + document.getId());
        }
    }
    
    @PreAuthorize("@permission.toAccess(#file, 'WRITE')")
    @RequestMapping(value = "documents/{id}",
                    method = RequestMethod.PUT)
    public ResponseEntity<Metadata> replaceDocument(
            @ActiveUser User user,
            @PathVariable("file") String id,
            @RequestParam(value = "message", defaultValue = "edit document") String commitMessage,
            @Valid @RequestBody Metadata document) throws DataRepositoryException {
        
        if(!id.equals(document.getId())) {
            throw new IllegalArgumentException("The document posted to this resource does not have the id: " + id);
        }
        
        repo.submitData(document.getId(), mapper.of(document))
                .commit(user, commitMessage);
        return new ResponseEntity<>(document, HttpStatus.OK);
    }
    
    @PreAuthorize("@permission.toAccess(#file, 'DOCUMENT_READ')")
    @RequestMapping( value ="documents/{file}",
                     method = RequestMethod.GET)
    @ResponseBody
    public Metadata readMetadata(
            @PathVariable("file") String file ) throws DataRepositoryException, IOException {
        return mapper.read(repo.getData(file), Metadata.class);
    }
    
    @PreAuthorize("@permission.toAccess(#file, #revision, 'DOCUMENT_READ')")
    @RequestMapping( value ="history/{revision}/{file}",
                     method = RequestMethod.GET)
    @ResponseBody
    public Metadata readMetadata(
            @PathVariable("file") String file,
            @PathVariable("revision") String revision,
            @RequestParam(value = "original", defaultValue = "false") boolean original ) throws DataRepositoryException, IOException {
        Metadata document = mapper.read(repo.getData(file, revision), Metadata.class);
        
        if(!original) {
            document = links.getInContext(document, revision);
        }
        
        return document;
    }
    
    @PreAuthorize("@permission.toAccess(#file, 'WRITE')")
    @RequestMapping(value = "documents/{file}",
            method = RequestMethod.DELETE)
    @ResponseBody
    public DataRevision<User> deleteDocument(
            @ActiveUser User user,
            @RequestParam(value = "message", defaultValue = "delete document") String reason,
            @PathVariable("file") String file) throws DataRepositoryException, IOException {
        return repo.deleteData(file).commit(user, reason);
    }
}
