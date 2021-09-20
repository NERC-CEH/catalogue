package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Data
@EqualsAndHashCode(callSuper = false)
public class ServiceAgreementModel extends RepresentationModel<ServiceAgreementModel> {
    private String id;
    private String title;
    private String depositorName;

    public ServiceAgreementModel(ServiceAgreement serviceAgreement) {
        this.id = serviceAgreement.getId();
        this.title = serviceAgreement.getTitle();
        this.depositorName = serviceAgreement.getDepositorName();
    }
}
