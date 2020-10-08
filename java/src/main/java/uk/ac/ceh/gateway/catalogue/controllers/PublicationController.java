package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.services.PublicationService;

@Slf4j
@ToString
@Controller
public class PublicationController {
    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
        log.info("Creating {}", this);
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}/publication", 
                    method =  RequestMethod.GET)
    @ResponseBody
    public HttpEntity<StateResource> currentPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) {
        return ResponseEntity.ok(publicationService.current(user, file)); 
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}/publication/{toState}", 
                    method =  RequestMethod.POST)
    @ResponseBody
    public HttpEntity<StateResource> transitionPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("toState") String toState) {
       return ResponseEntity.ok(publicationService.transition(user, file, toState));
    }
}