package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    private String fileNumber;
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
    //description
    private List<DescriptiveKeywords> descriptiveKeywords;
    private String lineage;
    private List<BoundingBox> areaOfStudy;

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return Optional.ofNullable(descriptiveKeywords)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(dk -> dk.getKeywords().stream())
                .collect(Collectors.toList());
    }

}
