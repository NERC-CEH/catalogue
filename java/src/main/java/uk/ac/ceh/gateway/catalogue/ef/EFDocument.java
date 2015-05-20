package uk.ac.ceh.gateway.catalogue.ef;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

/**
 *
 * @author cjohn
 */
@Data
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/ef.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class EFDocument implements MetadataDocument {
    private URI uri;
    private String description, title, id, type;
    private MetadataInfo metadata;
    
    @Override
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }
        
    @Override
    public void attachUri(URI uri) {
        setUri(uri);
    }

    @Override
    public List<String> getLocations() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<String> getTopics() {
        return Collections.EMPTY_LIST;
    }
}
