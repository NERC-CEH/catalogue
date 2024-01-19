package uk.ac.ceh.gateway.catalogue.model;

import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;

@Data
@AllArgsConstructor
public class TransparentProxy {
    private @NotNull final URI uri;
    private final MediaType desiredMediaType;

    public TransparentProxy(String url) throws URISyntaxException {
        this(url, null);
    }

    public TransparentProxy(String url, MediaType desiredMediaType) throws URISyntaxException {
        this(new URI(url), desiredMediaType);
    }
}
