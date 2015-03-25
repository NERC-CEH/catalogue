package uk.ac.ceh.gateway.catalogue.publication;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Value;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@ConvertUsing({
    @Template(called="html/publication.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Value
public class StateResource {
    private final String id, title, metadataTitle, metadataHref;
    private final Set<TransitionResource> transitions;

    public StateResource(State currentState, Set<PublishingRole> roles, UriComponentsBuilder builder, String metadataTitle, String metadataHref) {
        this.id = currentState.getId();
        this.title = currentState.getTitle();
        this.transitions = currentState.avaliableTransitions(roles)
            .stream()
            .map(transition -> {
                return new TransitionResource(currentState, transition, builder);
            })
            .collect(Collectors.toSet());
        this.metadataTitle = Objects.requireNonNull(Strings.emptyToNull(metadataTitle));
        this.metadataHref = Objects.requireNonNull(Strings.emptyToNull(metadataHref));
    } 
}