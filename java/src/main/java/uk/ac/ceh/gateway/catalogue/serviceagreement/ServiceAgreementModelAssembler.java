package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Service;

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
        val model =  new ServiceAgreementModel(serviceAgreement);
        log.debug("model: {}", model);
        return model;
    }
}
