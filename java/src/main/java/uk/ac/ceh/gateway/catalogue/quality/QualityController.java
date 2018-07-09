package uk.ac.ceh.gateway.catalogue.quality;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.CatalogueResults;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Results;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.stream.Collectors;

@Controller
@SuppressWarnings("unused")
@AllArgsConstructor
public class QualityController {
    private final MetadataQualityService metadataQualityService;
    private final MetadataListingService metadataListingService;

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
                    .map(id -> metadataQualityService.check(id))
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
}
