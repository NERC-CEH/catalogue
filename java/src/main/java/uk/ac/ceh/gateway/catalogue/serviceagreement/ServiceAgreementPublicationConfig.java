package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.publication.PublishingRole;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.Transition;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

@Configuration
public class ServiceAgreementPublicationConfig {

    public static final String draftToSubmittedId = "ttv9o";
    public static final String submittedToUnderReviewId = "er8pu";
    public static final String submittedToDraftId = "r18oq";
    public static final String underReviewToReadyForAgreementId = "jbre2";
    public static final String underReviewToDraftId = "l2leq";
    public static final String readyForAgreementToAgreedId = "g0r6d";
    public static final String readyForAgreementToDraftId = "7zirq";

    @Bean(name="serviceAgreementWorkflow")
    @Qualifier("service-agreement")
    public Workflow workflow() {

        // States
        State draft, submitted, underReview, readyForAgreement, agreed;
        draft = new State("draft", "Draft");
        submitted = new State("submitted", "Submitted");
        underReview = new State("under-review", "Under Review");
        readyForAgreement = new State("ready-for-agreement", "Ready for Agreement");
        agreed = new State("agreed", "Agreed");

        // Transitions
        Transition draftToSubmitted = Transition.builder()
            .toState(submitted)
            .id(draftToSubmittedId)
            .title("Submit")
            .helpText("Submit service agreement for review")
            .build();

        Transition submittedToUnderReview = Transition.builder()
            .toState(underReview)
            .id(submittedToUnderReviewId)
            .title("For approval")
            .helpText("Agree service agreement")
            .build();

        Transition submittedToDraft = Transition.builder()
            .toState(draft)
            .id(submittedToDraftId)
            .title("Edits required")
            .helpText("Move service agreement back to draft state")
            .build();

        Transition underReviewToReadyForAgreement = Transition.builder()
            .toState(readyForAgreement)
            .id(underReviewToReadyForAgreementId)
            .title("Ready for agreement")
            .helpText("Complete review of service agreement")
            .build();

        Transition underReviewToDraft = Transition.builder()
            .toState(draft)
            .id(underReviewToDraftId)
            .title("Edits required")
            .helpText("Move service agreement back to draft state")
            .build();

        Transition readyForAgreementToAgreed = Transition.builder()
            .toState(agreed)
            .id(readyForAgreementToAgreedId)
            .title("Agree Service Agreement")
            .helpText("Agree Service Agreement")
            .build();

        Transition readyForAgreementToDraft = Transition.builder()
            .toState(draft)
            .id(readyForAgreementToDraftId)
            .title("Edits required")
            .helpText("Move service agreement back to draft state")
            .build();

        // Roles
        PublishingRole publisher, depositor;
        publisher = new PublishingRole("publisher");
        depositor = new PublishingRole("depositor");

        // Add transitions to states
        submitted.addTransitions(publisher, ImmutableSet.of(submittedToUnderReview, submittedToDraft));
        underReview.addTransitions(publisher, ImmutableSet.of(underReviewToReadyForAgreement, underReviewToDraft));

        draft.addTransitions(depositor, ImmutableSet.of(draftToSubmitted));
        readyForAgreement.addTransitions(depositor, ImmutableSet.of(readyForAgreementToAgreed, readyForAgreementToDraft));

        // Add states to workflow
        Map<String, State> states = new ImmutableMap.Builder<String, State>()
            .put(draft.getId(), draft)
            .put(submitted.getId(), submitted)
            .put(underReview.getId(), underReview)
            .put(readyForAgreement.getId(), readyForAgreement)
            .put(agreed.getId(), agreed)
            .build();

        return new Workflow(states);
    }

}
