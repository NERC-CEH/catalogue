package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.TransitionResource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Data
@EqualsAndHashCode(callSuper = false)
@ConvertUsing({
        @Template(called = "html/service_agreement/service-agreement.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
@JsonIgnoreProperties(value = {"historical", "transitions", "stateTitle"})
public class ServiceAgreementModel extends RepresentationModel<ServiceAgreementModel> {

    private String id, title, depositReference, depositorName, depositorContactDetails, eidcName, eidcContactDetails,otherPoliciesOrLegislation, fileNumber,transferMethod, fileNamingConvention, policyExceptions, availability, useConstraints, supersededData, otherInfo, description, lineage;

    private List<ResponsibleParty> authors;

    private List<Funding> funding;

    private Keyword dataCategory;

    private List<File> files;

    private List<SupportingDoc> supportingDocs;

    private ResourceConstraint endUserLicence;

    private List<ResponsibleParty> ownersOfIpr;

    private List<Keyword> topicCategories, keywordsDiscipline, keywordsInstrument, keywordsObservedProperty,
        keywordsPlace, keywordsProject, keywordsTheme, keywordsOther, allKeywords;


    private List<BoundingBox> boundingBoxes;

    private String state;

    /*
     * FLAGS
     */
    //@JsonIgnore
    private boolean historical;
    private Set<TransitionResource> transitions;
    private String stateTitle;

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
        this.fileNamingConvention = serviceAgreement.getFileNamingConvention();
        this.files = serviceAgreement.getFiles();
        this.supportingDocs = serviceAgreement.getSupportingDocs();
        this.transferMethod = serviceAgreement.getTransferMethod();
        this.dataCategory = serviceAgreement.getDataCategory();
        this.policyExceptions = serviceAgreement.getPolicyExceptions();
        this.availability = serviceAgreement.getAvailability();
        this.endUserLicence = serviceAgreement.getEndUserLicence();
        this.ownersOfIpr = serviceAgreement.getOwnersOfIpr();
        this.useConstraints = serviceAgreement.getUseConstraints();
        this.supersededData = serviceAgreement.getSupersededData();
        this.otherInfo = serviceAgreement.getOtherInfo();
        this.topicCategories = serviceAgreement.getTopicCategories();
        this.keywordsDiscipline = serviceAgreement.getKeywordsDiscipline();
        this.keywordsTheme = serviceAgreement.getKeywordsTheme();
        this.keywordsOther = serviceAgreement.getKeywordsOther();
        this.allKeywords = serviceAgreement.getAllKeywords();
        this.description = serviceAgreement.getDescription();
        this.lineage = serviceAgreement.getLineage();
        this.boundingBoxes = serviceAgreement.getBoundingBoxes();
        this.historical = serviceAgreement.isHistorical();
        this.state = serviceAgreement.getState();
        StateResource currentStateResource = serviceAgreement.getCurrentStateResource();
        if (currentStateResource == null) {
            this.stateTitle = serviceAgreement.getState();
            this.transitions = Collections.emptySet();
        } else {
            this.stateTitle = currentStateResource.getTitle();
            this.transitions = currentStateResource.getTransitions();
        }
    }

    @SuppressWarnings("unused")
    public List<Link> getModelLinks(){
        return StreamSupport
            .stream(this.getLinks().spliterator(), false)
            .filter(link -> !link.getRel().value().equals("self"))
            .collect(Collectors.toList());
    }
}
