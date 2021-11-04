package uk.ac.ceh.gateway.catalogue.serviceagreement;

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
        @Template(called = "html/service-agreement.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
public class ServiceAgreementModel extends RepresentationModel<ServiceAgreementModel> {

    /*
    GENERAL
    */
    private String id;
    private String title;
    private String depositReference;
    private String depositorName;
    private String depositorContactDetails;
    private String eidcName;
    private String eidcContactDetails;

    /*
    ONE: Data Identification and Citation
    */
    private List<ResponsibleParty> authors;

    /*
    TWO: Policies & Legislation
   */
    private String otherPoliciesOrLegislation;

    /*
    THREE: The data
    */
    private String fileNumber;
    private List<File> files;
    private String transferMethod;
    private List<RelatedRecord> relatedDataHoldings;
    private Keyword dataCategory;

    /*
    FOUR: Supporting documentation
    */
    private List<String> supportingDocumentNames;
    private String contentIncluded;

    /*
    FIVE: Data retention
    */
    private String policyExceptions;

    /*
    SIX: Availability and access
    */
    private String availability;
    private String specificRequirements;
    private String otherServicesRequired;

    /*
    SEVEN: Licensing and IPR
    */
    private ResourceConstraint endUserLicence;
    private List<ResponsibleParty> ownersOfIpr;
    private String useConstraints;

    /*
    EIGHT: Superseding existing data (if applicable)
    */
    private String supersededMetadataId;
    private String supersededReason;

    /*
    NINE: Miscellaneous
    */
    private String otherInfo;

    /*
    TEN: Discovery metadata
    */
    private List<Keyword> allKeywords;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private String description;
    private String lineage;
    private List<BoundingBox> areaOfStudy;

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
        this.fileNumber = serviceAgreement.getFileNumber();
        this.files = serviceAgreement.getFiles();
        this.transferMethod = serviceAgreement.getTransferMethod();
        this.relatedDataHoldings = serviceAgreement.getRelatedDataHoldings();
        this.dataCategory = serviceAgreement.getDataCategory();
        this.supportingDocumentNames = serviceAgreement.getSupportingDocumentNames();
        this.contentIncluded = serviceAgreement.getContentIncluded();
        this.policyExceptions = serviceAgreement.getPolicyExceptions();
        this.availability = serviceAgreement.getAvailability();
        this.specificRequirements = serviceAgreement.getSpecificRequirements();
        this.otherServicesRequired = serviceAgreement.getOtherServicesRequired();
        this.endUserLicence = serviceAgreement.getEndUserLicence();
        this.ownersOfIpr = serviceAgreement.getOwnersOfIpr();
        this.useConstraints = serviceAgreement.getUseConstraints();
        this.supersededMetadataId = serviceAgreement.getSupersededMetadataId();
        this.supersededReason = serviceAgreement.getSupersededReason();
        this.otherInfo = serviceAgreement.getOtherInfo();
        this.allKeywords = serviceAgreement.getAllKeywords();
        this.descriptiveKeywords = serviceAgreement.getDescriptiveKeywords();
        this.description = serviceAgreement.getDescription();
        this.lineage = serviceAgreement.getLineage();
        this.areaOfStudy = serviceAgreement.getAreaOfStudy();
    }

    public List<Link> getModelLinks(){
        List<Link> actualList = StreamSupport
                .stream(this.getLinks().spliterator(), false)
                .filter(link -> !link.getRel().value().equals("self"))
                .collect(Collectors.toList());
        return actualList;
    }
}
