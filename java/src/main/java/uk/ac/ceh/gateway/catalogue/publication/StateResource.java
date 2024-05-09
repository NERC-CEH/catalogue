package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.base.Strings;
import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ConvertUsing({
    @Template(called="html/publication.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Value
public class StateResource {
    String id, title, metadataId, catalogue, basePath;
    Set<TransitionResource> transitions;

    public StateResource(State currentState, Set<PublishingRole> roles, String metadataId, String catalogue, String basePath) {
        this.id = currentState.getId();
        this.title = currentState.getTitle();
        this.catalogue = catalogue;
        this.transitions = currentState.availableTransitions(roles)
            .stream()
            .map(transition -> new TransitionResource(currentState, transition))
            .collect(Collectors.toSet());
        this.metadataId = Objects.requireNonNull(Strings.emptyToNull(metadataId));
        this.basePath = basePath;
    }
}
