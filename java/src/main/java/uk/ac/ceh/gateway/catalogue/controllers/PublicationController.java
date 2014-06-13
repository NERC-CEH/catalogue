package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.services.PublicationService;

@Controller
public class PublicationController {
    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }
    
    //@PreAuthorize("@permission.toAccess(#file, 'READ')")
    @RequestMapping(value = "documents/{file}/publication", 
                    method =  RequestMethod.GET)
    @ResponseBody
    public HttpEntity<State> currentPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) {
        return new ResponseEntity<>(publicationService.current(user, file), HttpStatus.OK); 
    }
    
    @PreAuthorize("@permission.toAccess(#file, 'WRITE')")
    @RequestMapping(value = "documents/{file}/publication/{transition}", 
                    method =  RequestMethod.PUT)
    @ResponseBody
    public State transitionPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("transition") String transition) {
       return publicationService.transition(user, file, transition);
    }

}