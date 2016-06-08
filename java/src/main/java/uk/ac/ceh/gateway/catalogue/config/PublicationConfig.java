package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.publication.PublishingRole;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.Transition;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

@Configuration
public class PublicationConfig {

    @Bean
    public Workflow workflow() {

        // States
        State draft, pending, published;
        draft = new State("draft", "Draft");
        pending = new State("pending", "Pending Publication");
        published = new State("published", "Published");

        // Transitions
        Transition draftToPending = Transition.builder()
            .toState(pending)
            .id("ykhm7b")
            .title("Request publication of document")
            .helpText("Requesting publication of this document. A Publisher will review this record for quality.")
            .build();

        Transition pendingToDraft = Transition.builder()
            .toState(draft)
            .id("gtkzpq")
            .title("Retract document to Draft")
            .helpText("Retract this document to Draft if you no longer want it published.")
            .build();

        Transition pendingToPublished = Transition.builder()
            .toState(published)
            .id("re4vkb")
            .title("Publish document")
            .helpText("Only publish document after quality checks have been completed.")
            .build();

        Transition publishedToDraft = Transition.builder()
            .toState(draft)
            .id("qtak5r")
            .title("Retract document from Published")
            .helpText("Only retract this document from Published after you have checked it is not a DOI landing page. Create a JIRA issue to track what happens.")
            .build();

        // Roles
        PublishingRole editor, publisher;
        editor = new PublishingRole(DocumentController.EDITOR_ROLE);
        publisher = new PublishingRole(DocumentController.PUBLISHER_ROLE);

        // Add transitions to states
        draft.addTransitions(editor, ImmutableSet.of(draftToPending));
        draft.addTransitions(publisher, ImmutableSet.of(draftToPending));

        pending.addTransitions(editor, ImmutableSet.of(pendingToDraft));
        pending.addTransitions(publisher, ImmutableSet.of(pendingToDraft, pendingToPublished));

        published.addTransitions(publisher, ImmutableSet.of(publishedToDraft));

        // Add states to workflow
        Map<String, State> states = new ImmutableMap.Builder<String, State>()
                                        .put(draft.getId(), draft)
                                        .put(pending.getId(), pending)
                                        .put(published.getId(), published)
                                        .build();

        return new Workflow(states);
    }

}
