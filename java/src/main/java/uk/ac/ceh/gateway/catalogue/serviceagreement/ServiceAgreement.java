package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    private List<Keyword> topicCategories, keywordsDiscipline, keywordsInstrument, keywordsObservedProperty,
        keywordsPlace, keywordsProject, keywordsTheme, keywordsOther;

    private List<BoundingBox> boundingBoxes;

    /*
     * FLAGS
     */
    @JsonIgnore
    private boolean historical;

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return Stream.of(
                Optional.ofNullable(keywordsDiscipline).orElse(Collections.emptyList()),
                Optional.ofNullable(keywordsInstrument).orElse(Collections.emptyList()),
                Optional.ofNullable(keywordsObservedProperty).orElse(Collections.emptyList()),
                Optional.ofNullable(keywordsPlace).orElse(Collections.emptyList()),
                Optional.ofNullable(keywordsProject).orElse(Collections.emptyList()),
                Optional.ofNullable(keywordsTheme).orElse(Collections.emptyList()),
                Optional.ofNullable(keywordsOther).orElse(Collections.emptyList())
            )
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

}
