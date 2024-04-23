package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;

@Slf4j
@ToString
@RestController
public class DocumentPublicationController {
    private final PublicationService publicationService;

    public DocumentPublicationController(@Qualifier("document") PublicationService publicationService) {
        this.publicationService = publicationService;
        log.info("Creating {}", this);
    }

    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("documents/{file}/publication")
    public HttpEntity<StateResource> currentPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file) {
        return ResponseEntity.ok(publicationService.current(user, file));
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PostMapping("documents/{file}/publication/{toState}")
    public HttpEntity<StateResource> transitionPublication(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("toState") String toState) {
        log.debug("Transition {} to {}", file, toState);
        return ResponseEntity.ok(publicationService.transition(user, file, toState));
    }
}
