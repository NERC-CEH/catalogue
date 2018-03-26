package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@ConvertUsing({
    @Template(called="html/publication.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Value
public class StateResource {
    private final String id, title, metadataId, catalogue;
    private final Set<TransitionResource> transitions;

    public StateResource(State currentState, Set<PublishingRole> roles, String metadataId, String catalogue) {
        this.id = currentState.getId();
        this.title = currentState.getTitle();
        this.catalogue = catalogue;
        this.transitions = currentState.avaliableTransitions(roles)
            .stream()
            .map(transition -> {
                return new TransitionResource(currentState, transition);
            })
            .collect(Collectors.toSet());
        this.metadataId = Objects.requireNonNull(Strings.emptyToNull(metadataId));
    } 
}