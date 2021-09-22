package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    private final ServiceAgreementModelAssembler serviceAgreementModelAssembler;

    public ServiceAgreementController(
        ServiceAgreementSearch search,
        ServiceAgreementService serviceAgreementService,
        ServiceAgreementModelAssembler serviceAgreementModelAssembler
    ) {
        this.search = search;
        this.serviceAgreementService = serviceAgreementService;
        this.serviceAgreementModelAssembler = serviceAgreementModelAssembler;
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanEdit(#id)")
    @PutMapping("{id}")
    public ServiceAgreementModel create(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @RequestParam("catalogue") String catalogue,
        @RequestBody ServiceAgreement serviceAgreement
        ) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("CREATE {}", id);
            serviceAgreement.setId(id);
            serviceAgreementService.save(user, id, catalogue, serviceAgreement);
            return serviceAgreementModelAssembler.toModel(serviceAgreement);
        }else{
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
    }

    @PreAuthorize("@permission.userCanView(#id)")
    @GetMapping("{id}")
    public ServiceAgreementModel get(
        @PathVariable("id") String id
    ) {
        log.info("GET {}", id);
        val serviceAgreement = serviceAgreementService.get(id);
        return serviceAgreementModelAssembler.toModel(serviceAgreement);
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
