package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Data
@EqualsAndHashCode(callSuper = false)
@ConvertUsing({
        @Template(called = "html/service_agreement/service-agreement.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
public class ServiceAgreementModel extends RepresentationModel<ServiceAgreementModel> {

    private String id, title, depositReference, depositorName, depositorContactDetails, eidcName, eidcContactDetails,otherPoliciesOrLegislation, transferMethod, relatedDataHoldings, contentIncluded, fileNumber, policyExceptions, availability, useConstraints, supersededData, otherInfo, description, lineage;

    private List<ResponsibleParty> authors;

    private List<Funding> funding;

    private List<File> files;

    private Keyword dataCategory;

    private List<String> supportingDocumentNames;

    private ResourceConstraint endUserLicence;
   
    private List<ResponsibleParty> ownersOfIpr;
    
    private List<Keyword> allKeywords;

    private List<DescriptiveKeywords> descriptiveKeywords;

    private List<BoundingBox> areaOfStudy;

    /*
    FLAGS
    */
    @JsonIgnore
    private boolean historical;

    public ServiceAgreementModel(ServiceAgreement serviceAgreement) {
        this.id = serviceAgreement.getId();
        this.title = serviceAgreement.getTitle();
        this.depositReference = serviceAgreement.getDepositReference();
        this.depositorName = serviceAgreement.getDepositorName();
        this.depositorContactDetails = serviceAgreement.getDepositorContactDetails();
        this.eidcName = serviceAgreement.getEidcName();
        this.eidcContactDetails = serviceAgreement.getEidcContactDetails();
        this.authors = serviceAgreement.getAuthors();
        this.otherPoliciesOrLegislation = serviceAgreement.getOtherPoliciesOrLegislation();
        this.funding = serviceAgreement.getFunding();
        this.fileNumber = serviceAgreement.getFileNumber();
        this.files = serviceAgreement.getFiles();
        this.transferMethod = serviceAgreement.getTransferMethod();
        this.relatedDataHoldings = serviceAgreement.getRelatedDataHoldings();
        this.dataCategory = serviceAgreement.getDataCategory();
        this.supportingDocumentNames = serviceAgreement.getSupportingDocumentNames();
        this.contentIncluded = serviceAgreement.getContentIncluded();
        this.policyExceptions = serviceAgreement.getPolicyExceptions();
        this.availability = serviceAgreement.getAvailability();
        this.endUserLicence = serviceAgreement.getEndUserLicence();
        this.ownersOfIpr = serviceAgreement.getOwnersOfIpr();
        this.useConstraints = serviceAgreement.getUseConstraints();
        this.supersededData = serviceAgreement.getSupersededData();
        this.otherInfo = serviceAgreement.getOtherInfo();
        this.allKeywords = serviceAgreement.getAllKeywords();
        this.descriptiveKeywords = serviceAgreement.getDescriptiveKeywords();
        this.description = serviceAgreement.getDescription();
        this.lineage = serviceAgreement.getLineage();
        this.areaOfStudy = serviceAgreement.getAreaOfStudy();
        this.historical = serviceAgreement.isHistorical();
    }

    @SuppressWarnings("unused")
    public List<Link> getModelLinks(){
        return StreamSupport
            .stream(this.getLinks().spliterator(), false)
            .filter(link -> !link.getRel().value().equals("self"))
            .collect(Collectors.toList());
    }
}
