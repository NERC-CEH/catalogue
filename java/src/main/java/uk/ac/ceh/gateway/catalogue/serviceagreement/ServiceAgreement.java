package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)

public class ServiceAgreement extends AbstractMetadataDocument {

    /*
    GENERAL
    */
    //id
    //title
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
    private List<File> files;
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
    //keywords
    //description
    private String lineage;
    private String areaOfStudy;
}
