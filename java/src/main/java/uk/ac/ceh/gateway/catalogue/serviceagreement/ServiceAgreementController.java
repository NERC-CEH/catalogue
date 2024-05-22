package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
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
    @PostMapping("{id}")
    public ServiceAgreementModel create(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id,
            @RequestParam("catalogue") String catalogue,
            @RequestBody ServiceAgreement serviceAgreement
            ) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("creating service agreement {}", id);
            val newlyCreated = serviceAgreementService.create(
                    user,
                    id,
                    catalogue,
                    serviceAgreement
                    );
            return serviceAgreementModelAssembler.toModel(newlyCreated);
        } else {
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
            }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @PutMapping("{id}")
    public ServiceAgreementModel update(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id,
            @RequestBody ServiceAgreement serviceAgreement
            ) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("updating service agreement {}", id);
            val newlyUpdated = serviceAgreementService.update(
                    user,
                    id,
                    serviceAgreement
                    );
            return serviceAgreementModelAssembler.toModel(newlyUpdated);
        } else {
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
            }

    @PreAuthorize("@permission.userCanViewServiceAgreement(#id)")
    @GetMapping("{id}")
    public ServiceAgreementModel get(
            @ActiveUser CatalogueUser user,
            @PathVariable("id") String id
            ) {
        log.info("GET {}", id);
        val serviceAgreement = serviceAgreementService.get(user, id);
        return serviceAgreementModelAssembler.toModel(serviceAgreement);
            }

    @PreAuthorize("@permission.userCanDeleteServiceAgreement(#id)")
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
    @SneakyThrows
    public List<ServiceAgreementSolrIndex> search(
            @RequestParam(value = "query", defaultValue = "*") String query
            ) {
        return search.query(query);
            }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @PostMapping("{id}/submit")
    public RedirectView submitServiceAgreement(@ActiveUser CatalogueUser user,
            @PathVariable("id") String id
            ) {
        log.info("SUBMITTING SERVICE AGREEMENT {}", id);
        serviceAgreementService.submitServiceAgreement(user, id);
        return new RedirectView("/service-agreement/" + id);
            }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @PostMapping("{id}/publish")
    public RedirectView publishServiceAgreement(@ActiveUser CatalogueUser user,
            @PathVariable("id") String id
            ) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("PUBLISHING SERVICE AGREEMENT {}", id);
            serviceAgreementService.publishServiceAgreement(user, id);
            return new RedirectView("/service-agreement/" + id);
        } else {
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
            }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @PostMapping("{id}/add-editor")
    public RedirectView giveDepositorEditPermission(@ActiveUser CatalogueUser user,
            @PathVariable("id") String id
            ) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("GIVING DEPOSITOR EDIT PERMISSION FOR SERVICE AGREEMENT {}", id);
            serviceAgreementService.giveDepositorEditPermission(user, id);
            return new RedirectView("/service-agreement/" + id);
        } else {
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
            }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @GetMapping("{id}/history")
    public History getHistory(@PathVariable("id") String id) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("GETTING SERVICE AGREEMENT {} HISTORY", id);

            return serviceAgreementService.getHistory(id);

        } else {
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
    }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @GetMapping("{id}/version/{version}")
    public ServiceAgreementModel getPreviousVersion(
            @PathVariable("id") String id,
            @PathVariable String version
            ) {
        if (serviceAgreementService.metadataRecordExists(id)) {
            log.info("GETTING SERVICE AGREEMENT {} HISTORY", id);
            val serviceAgreement = serviceAgreementService.getPreviousVersion(id, version);
            return serviceAgreementModelAssembler.toModel(serviceAgreement);
        }else{
            throw new ResourceNotFoundException("Metadata record does not exist");
        }
            }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @RequestMapping(method = RequestMethod.GET, value = "{id}/permission")
    @ResponseBody
    public HttpEntity<ServiceAgreementPermissionResource> currentPermission (
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id
    ) {
        log.info("GETTING SERVICE AGREEMENT PERMISSIONS");
        return ResponseEntity.ok(
            new ServiceAgreementPermissionResource(
                serviceAgreementService.get(user, id)
            )
        );
    }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @RequestMapping(method =  RequestMethod.PUT, value = "{id}/permission")
    @ResponseBody
    public HttpEntity<ServiceAgreementPermissionResource> updatePermission (
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @RequestBody ServiceAgreementPermissionResource permissionResource
    ) {
        log.info("UPDATING SERVICE AGREEMENT PERMISSIONS");
        MetadataInfo original = serviceAgreementService.get(user, id).getMetadata();
        MetadataInfo updated = permissionResource.updatePermissions(original);
        serviceAgreementService.updateMetadata(user, id, updated);
        return ResponseEntity.ok(
            new ServiceAgreementPermissionResource(
                serviceAgreementService.get(user, id)
            )
        );
    }

    @PreAuthorize("@permission.userCanEditServiceAgreement(#id)")
    @PostMapping("{id}/form-publication/{toState}")
    public RedirectView formTransitionPublication(
        @ActiveUser CatalogueUser user,
        @PathVariable("id") String id,
        @PathVariable("toState") String toState) {

        val nextStateResource = serviceAgreementService.transitState(user, id, toState);
        return new RedirectView("/service-agreement/" + id);
    }

}
