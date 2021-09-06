package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

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

    @GetMapping("{id}")
    public MetadataDocument create(
            @ActiveUser CatalogueUser user,
            @RequestParam("catalogue") String catalogue,
            @PathVariable("id") String id
    ) {
        AbstractMetadataDocument serviceAgreement = null;
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("CREATE {}", id);
            serviceAgreement = new ServiceAgreement().setTitle(id);
            serviceAgreementService.save(user, id, catalogue, serviceAgreement);
        }
        return serviceAgreement;
    }

    //TODO: re-enable security
//    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("{id}")
    public DataDocument get(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        log.info("GET {}", id);
        return serviceAgreementService.get(id);
    }

    //TODO: re-enable security
//    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("{id}")
    public DataRevision<CatalogueUser> delete(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
    ) {
        log.info("DELETE {}", id);
        return serviceAgreementService.delete(user, id);
    }

    //TODO: Add security. who should be able to access search?
    @GetMapping
    public List<ServiceAgreementSolrIndex> search(
            @RequestParam(value = "query", defaultValue = "*") String query
    ) {
        return search.query(query);
    }

}
