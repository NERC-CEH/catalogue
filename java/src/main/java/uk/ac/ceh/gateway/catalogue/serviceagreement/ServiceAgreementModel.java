package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.List;

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
    private String dataFiles;
    private List<String> fileNames;
    private List<String> fileFormats;
    private String fileSize;
    private String transferMethod;
    private List<RelatedRecord> relatedDataHoldings;
    private String dataCategory;

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
    private String endUserLicence;
    private String ownerOfIpr;
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
    private List<Keyword> keywords;
    private String description;
    private String lineage;
    private String areaOfStudy;

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
        this.dataFiles = serviceAgreement.getDataFiles();
        this.fileNames = serviceAgreement.getFileNames();
        this.fileFormats = serviceAgreement.getFileFormats();
        this.fileSize = serviceAgreement.getFileSize();
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
        this.ownerOfIpr = serviceAgreement.getOwnerOfIpr();
        this.useConstraints = serviceAgreement.getUseConstraints();
        this.supersededMetadataId = serviceAgreement.getSupersededMetadataId();
        this.supersededReason = serviceAgreement.getSupersededReason();
        this.otherInfo = serviceAgreement.getOtherInfo();
        this.keywords = serviceAgreement.getKeywords();
        this.description = serviceAgreement.getDescription();
        this.lineage = serviceAgreement.getLineage();
        this.areaOfStudy = serviceAgreement.getAreaOfStudy();
    }
}
