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

    private String depositReference, depositorName, depositorContactDetails, eidcName, eidcContactDetails, otherPoliciesOrLegislation, fileNumber, transferMethod, fileNamingConvention, policyExceptions, availability, useConstraints, supersededData, otherInfo, description, lineage;

    private List<ResponsibleParty> authors;

    private List<Funding> funding;

    private Keyword dataCategory;

    private List<File> files;

    private List<SupportingDoc> supportingDocs;

    private ResourceConstraint endUserLicence;

    private List<ResponsibleParty> ownersOfIpr;

    private List<DescriptiveKeywords> descriptiveKeywords;

    private List<BoundingBox> areaOfStudy;

    /*
    FLAGS
    */
    @JsonIgnore
    private boolean historical;

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
