package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;

import java.util.List;

@Profile("service-agreement")
@Slf4j
@RestController
@RequestMapping("service-agreement")
public class ServiceAgreementController {

    private final ServiceAgreementSearch search;
    private final ServiceAgreementService serviceAgreementService;

    public ServiceAgreementController(ServiceAgreementSearch search, ServiceAgreementService serviceAgreementService) {
        this.search = search;
        this.serviceAgreementService = serviceAgreementService;
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanEdit(#id)")
    @PostMapping("{id}")
    public ServiceAgreement create(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @RequestParam("catalogue") String catalogue,
        @RequestBody ServiceAgreement serviceAgreement
        ) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("CREATE {}", id);
            serviceAgreement.setId(id);
            serviceAgreementService.save(user, id, catalogue, serviceAgreement);
            return serviceAgreement;
        }else{
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
    }

    @PreAuthorize("@permission.userCanView(#id)")
    @GetMapping("{id}")
    public ServiceAgreement get(
        @PathVariable("id") String id
    ) {
        log.info("GET {}", id);
        return serviceAgreementService.get(id);
    }

    @PreAuthorize("@permission.userCanDelete(#id)")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        log.info("DELETE {}", id);
        serviceAgreementService.delete(user, id);
    }

    @PreAuthorize("@permission.userIsAdmin()")
    @GetMapping
    public List<ServiceAgreementSolrIndex> search(
            @RequestParam(value = "query", defaultValue = "*") String query
    ) {
        return search.query(query);
    }

}
