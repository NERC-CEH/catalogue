package uk.ac.ceh.gateway.catalogue.controllers;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.services.PublicationService;

@Controller
public class PublicationController {
    private final PublicationService publicationService;

    @Autowired
    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}/publication", 
                    method =  RequestMethod.GET)
    @ResponseBody
    public HttpEntity<StateResource> currentPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file, 
            HttpServletRequest request) {
        return ResponseEntity.ok(publicationService.current(user, file, getTransitionUriBuilder(request))); 
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}/publication/{toState}", 
                    method =  RequestMethod.POST)
    @ResponseBody
    public HttpEntity<StateResource> transitionPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("toState") String toState, 
            HttpServletRequest request) {
       return ResponseEntity.ok(publicationService.transition(user, file, toState, getTransitionUriBuilder(request, file)));
    }
    
    private UriComponentsBuilder getTransitionUriBuilder(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequest(request).path("/{transitionId}");
    }
    
    private UriComponentsBuilder getTransitionUriBuilder(HttpServletRequest request, String file) {
        String path = String.format("/documents/%s/publication/{transitionId}", file);
        return ServletUriComponentsBuilder.fromContextPath(request).path(path);
    }

}