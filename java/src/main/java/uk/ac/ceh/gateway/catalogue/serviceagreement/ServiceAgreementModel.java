package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@EqualsAndHashCode(callSuper = false)
@ConvertUsing({
        @Template(called = "html/service-agreement.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
public class ServiceAgreementModel extends RepresentationModel<ServiceAgreementModel> {
    private String id;
    private String title;

    private String depositReference;
    private String depositorName;
    private String depositorContactDetails;
    private String eidcName;
    private String eidcContactDetails;

    public ServiceAgreementModel(ServiceAgreement serviceAgreement) {
        this.id = serviceAgreement.getId();
        this.title = serviceAgreement.getTitle();

        this.depositReference = serviceAgreement.getDepositReference();
        this.depositorContactDetails = serviceAgreement.getDepositorContactDetails();
        this.eidcName = serviceAgreement.getEidcName();
        this.eidcContactDetails = serviceAgreement.getEidcContactDetails();
    }
}
