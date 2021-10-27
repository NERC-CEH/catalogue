package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Profile("service-agreement")
@Slf4j
@Service
public class ServiceAgreementModelAssembler extends RepresentationModelAssemblerSupport<ServiceAgreement, ServiceAgreementModel> {

    public ServiceAgreementModelAssembler() {
        super(ServiceAgreementController.class, ServiceAgreementModel.class);
        log.info("Creating");
    }

    @Override
    public ServiceAgreementModel toModel(ServiceAgreement serviceAgreement) {
        return createModelWithId(
            serviceAgreement.getId(),
            serviceAgreement
        );
    }

    @Override
    protected ServiceAgreementModel instantiateModel(ServiceAgreement serviceAgreement) {
        Link link = linkTo(methodOn(ServiceAgreementController.class).populateGeminiDocument(null, serviceAgreement.getId())).withRel("populate");
        val model =  new ServiceAgreementModel(serviceAgreement);
        model.add(link);
        log.debug("model: {}", model);
        return model;
    }

}
