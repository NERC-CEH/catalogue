package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.util.List;

@Profile("service-agreement")
@Slf4j
@RestController
@RequestMapping("service-agreement")
public class ServiceAgreementController {
    private final ServiceAgreementSearch search;

    public ServiceAgreementController(ServiceAgreementSearch search) {
        this.search = search;
        log.info("Creating");
    }

//    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("{id}")
    public MetadataDocument get(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id
    ) {
        log.info("GET {}", id);
        return new ServiceAgreement().setTitle("Service Agreement " + id);
    }

    @GetMapping
    public List<ServiceAgreementSolrIndex> search(
        @RequestParam(value = "query", defaultValue = "*") String query
    ) {
        return search.query(query);
    }
}
