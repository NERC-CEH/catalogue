package uk.ac.ceh.gateway.catalogue.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.publication.PublishingRole;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.Transition;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

@Configuration
public class PublicationConfig {
    
    @Bean
    public Workflow workflow() {
        
        // States
        State draft, pending, publik;
        draft = new State("draft", "Draft");
        pending = new State("pending", "Pending Publication");
        publik = new State("public", "Public");
        
        // Transitions
        Transition draftToPending = Transition.builder()
            .toState(pending)
            .title("Request publication of document")
            .helpText("Requesting publication of this document will notify a Publisher that you want this document to be made public. A Publisher will review this record for quality against CEH QA 2.4c, 3.45 and 76.4a rev9.")
            .confirmationQuestion("Submit \"{title (first 100 characters)}\" for publication?")
            .build();
        
        Transition pendingToDraft = Transition.builder()
            .toState(draft)
            .title("Retract document to Draft")
            .helpText("Retract this document to Daft if you no longer want it be made public.")
            .confirmationQuestion("Stop publication of \"{title (first 100 characters)}\"?")
            .build();
        
        Transition pendingToPublic = Transition.builder()
            .toState(publik)
            .title("Publish Document")
            .helpText("Only publish document after Quality Check 2, 2.4a and 4.ii have been completed.")
            .confirmationQuestion("Make \"{title (first 100 characters)}\" public?")
            .build();
        
        Transition publicToDraft = Transition.builder()
            .toState(draft)
            .title("Retract document from Public")
            .helpText("Only retract this document from Public after you have checked it is not a DOI landing page, create a JIRA issue to track what happens.")
            .confirmationQuestion("Remove public access to \"{title (first 100 characters)}\"?")
            .build();
        
        // Roles
        PublishingRole editor, publisher;
        editor = new PublishingRole("ROLE_EDITOR");
        publisher = new PublishingRole("ROLE_PUBLISHER");
        
        // Add transitions to states
        draft.addTransitions(editor, ImmutableSet.of(draftToPending));
        draft.addTransitions(publisher, ImmutableSet.of(draftToPending));
        
        pending.addTransitions(editor, ImmutableSet.of(pendingToDraft));
        pending.addTransitions(publisher, ImmutableSet.of(pendingToDraft, pendingToPublic));
        
        publik.addTransitions(publisher, ImmutableSet.of(publicToDraft));
        
        // Add states to workflow
        Map<String, State> states = new ImmutableMap.Builder<String, State>()
                                        .put(draft.getId(), draft)
                                        .put(pending.getId(), pending)
                                        .put(publik.getId(), publik)
                                        .build();
        
        return new Workflow(states);
    }

}