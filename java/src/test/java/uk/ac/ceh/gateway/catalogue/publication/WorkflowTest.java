package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.config.DocumentPublicationConfig;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class WorkflowTest {
    private final PublishingRole editor, publisher;
    private final Workflow workflow;

    public WorkflowTest() {
        //Given
        workflow = new DocumentPublicationConfig().workflow();
        editor = new PublishingRole("editor");
        publisher = new PublishingRole("publisher");
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
    public void editorTransitionDraftToPending() {
        //Given
        MetadataInfo original = MetadataInfo.builder().state("draft").build();

        final State currentState = workflow.currentState(original);
        final Transition toPending = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(editor)), "pending");

        //When
        MetadataInfo transitioned = workflow.transitionDocumentState(original, ImmutableSet.of(editor), toPending);

        //Then
        assertThat("state should be pending", transitioned.getState(), equalTo("pending"));
        assertThat("transitioned should not be equal to original", transitioned, not(equalTo(original)));
    }

    @Test
    public void editorCannotTransitionPendingToPublished() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("pending").build();

        final State currentState = workflow.currentState(info);
        final Transition toPublished = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(editor)), "published");

        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor), toPublished);

        //Then
        assertThat("state should be pending", info.getState(), equalTo("pending"));
    }

    @Test
    public void publisherCanTransitionPendingToPublished() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("pending").catalogue("eidc").build();

        final State currentState = workflow.currentState(info);
        final Transition toPublished = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(publisher)), "published");

        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor, publisher), toPublished);

        //Then
        assertThat("state should be published", info.getState(), equalTo("published"));
    }

    @Test
    public void editorCanNotTransitionFromPublishedToDraft() {
        //Given
        MetadataInfo info = MetadataInfo.builder().state("published").build();;

        final State currentState = workflow.currentState(info);
        final Transition toDraft = getTransitionTo(currentState.availableTransitions(ImmutableSet.of(editor)), "draft");

        //When
        info = workflow.transitionDocumentState(info, ImmutableSet.of(editor), toDraft);

        //Then
        assertThat("state should be published", info.getState(), equalTo("published"));
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
