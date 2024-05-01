package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;

@Profile("service-agreement")
@Slf4j
@ToString
@RestController
public class ServiceAgreementPublicationController {
    private final PublicationService publicationService;

    public ServiceAgreementPublicationController(
        @Qualifier("service-agreement") PublicationService publicationService
    ) {
        this.publicationService = publicationService;
        log.info("Creating {}", this);
    }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#file)")
    @GetMapping("service-agreement/{file}/publication")
    public HttpEntity<StateResource> currentPublication(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file) {
        return ResponseEntity.ok(publicationService.current(user, file));
    }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#file)")
    @PostMapping("service-agreement/{file}/publication/{toState}")
    public HttpEntity<StateResource> transitionPublication(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @PathVariable("toState") String toState) {
        log.debug("Transition {} to {}", file, toState);
        return ResponseEntity.ok(publicationService.transition(user, file, toState));
    }
}
