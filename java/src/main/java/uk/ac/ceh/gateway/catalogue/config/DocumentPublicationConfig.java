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
public class DocumentPublicationConfig {

    @Bean(name="documentWorkflow")
    @Qualifier("document")
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
            .title("Request publication")
            .helpText("Request publication - a Publisher will review it for quality before final publication")
            .build();

        Transition pendingToDraft = Transition.builder()
            .toState(draft)
            .id("gtkzpq")
            .title("Retract record")
            .helpText("Retract this record if you no longer want it published")
            .build();

        Transition pendingToPublished = Transition.builder()
            .toState(published)
            .id("re4vkb")
            .title("Publish record")
            .helpText("Only publish document after quality checks have been completed")
            .build();

        Transition publishedToDraft = Transition.builder()
            .toState(draft)
            .id("qtak5r")
            .title("Retract record")
            .helpText("Retract this record if you no longer want it published")
            .build();

        // Roles
        PublishingRole editor, publisher;
        editor = new PublishingRole("editor");
        publisher = new PublishingRole("publisher");

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
