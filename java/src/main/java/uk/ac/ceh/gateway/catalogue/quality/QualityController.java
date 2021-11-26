package uk.ac.ceh.gateway.catalogue.quality;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.stream.Collectors;

@Controller
@Slf4j
@ToString
public class QualityController {
    private final MetadataQualityService metadataQualityService;
    private final MetadataListingService metadataListingService;

    public QualityController(
            MetadataQualityService metadataQualityService,
            MetadataListingService metadataListingService
    ) {
        this.metadataQualityService = metadataQualityService;
        this.metadataListingService = metadataListingService;
        log.info("Creating {}", this);
    }

    @PreAuthorize("@permission.userCanEditRestrictedFields(#catalogue)")
    @GetMapping(value = "{catalogue}/quality", produces = "application/json")
    @ResponseBody
    public HttpEntity<CatalogueResults> quality(
        @ActiveUser CatalogueUser user,
        @PathVariable("catalogue") String catalogue
    ) {
        return ResponseEntity.ok(
            new CatalogueResults(
                metadataListingService.getPublicDocumentsOfCatalogue(catalogue).stream()
                    .map(metadataQualityService::check)
                    .collect(Collectors.toList())
            )
        );
    }

    @PreAuthorize("@permission.userCanEditRestrictedFields(#catalogue)")
    @GetMapping(value = "{catalogue}/quality/{id}", produces = "application/json")
    @ResponseBody
    public HttpEntity<Results> quality(
        @ActiveUser CatalogueUser user,
        @PathVariable("catalogue") String catalogue,
        @PathVariable("id") String id
    ) {
        return ResponseEntity.ok(metadataQualityService.check(id));
    }

    public MetadataListingService getMetadataListingService() {
        return metadataListingService;
    }
}
