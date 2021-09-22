package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@ConvertUsing({
        @Template(called = "html/service-agreement.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
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
