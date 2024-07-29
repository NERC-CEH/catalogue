package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.config.ServiceAgreementPublicationConfig;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.publication.PublishingRole;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.Transition;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class ServiceAgreementWorkflowTest {
    private final PublishingRole publisher, depositor;
    private final Workflow workflow;

    public ServiceAgreementWorkflowTest() {
        //Given
        workflow = new ServiceAgreementPublicationConfig().workflow();
        publisher = new PublishingRole("publisher");
        depositor = new PublishingRole("depositor");
    }

    @Test
    public void getCurrentStateOfDocumentInDraft() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("draft").build();

        //When
        final State currentState = workflow.currentState(info);

        //Then
        assertThat("state should be draft", currentState.getId(), equalTo("draft"));
    }

    @Test
    public void publisherCanTransitionDraftToSubmitted() {
        //Given
        MetadataInfo original = MetadataInfo.builder().state("draft").build();

        final State currentState = workflow.currentState(original);
        final Transition toSubmitted = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(depositor)), "submitted");

        //When
        MetadataInfo transitioned = workflow.transitionDocumentState(original, ImmutableSet.of(depositor), toSubmitted);

        //Then
        assertThat("state should be submitted", transitioned.getState(), equalTo("submitted"));
        assertThat("transitioned should not be equal to original", transitioned, not(equalTo(original)));
    }

    @Test
    public void publisherCanTransitionPendingToUnderReview() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("submitted").catalogue("eidc").build();

        final State currentState = workflow.currentState(info);
        final Transition toUnderReview = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(publisher)), "under-review");

        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(publisher), toUnderReview);

        //Then
        assertThat("state should be published", info.getState(), equalTo("under-review"));
    }

    public void publisherCanTransitionUnderReviewToReadyForAgreement() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("under-review").catalogue("eidc").build();

        final State currentState = workflow.currentState(info);
        final Transition toReadyForAgreement = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(publisher)), "ready-for-agreement");

        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(publisher), toReadyForAgreement);

        //Then
        assertThat("state should be published", info.getState(), equalTo("ready-for-agreement"));
    }

    @Test
    public void editorCanNotTransitionFromDraftToUnderReview() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("draft").build();;

        final State currentState = workflow.currentState(info);
        final Transition toDraft = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(publisher)), "under-review");

        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(publisher), toDraft);

        //Then
        assertThat("state should be published", info.getState(), equalTo("draft"));
    }

    private Transition getTransitionTo(Set<Transition> transitions, String state) {
        Transition toReturn = Transition.UNKNOWN_TRANSITION;
        for (Transition transition : transitions) {
            if (transition.getToState().getId().equalsIgnoreCase(state)) {
                toReturn = transition;
            }
        }
        return toReturn;
    }
}
