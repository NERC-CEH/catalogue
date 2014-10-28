package uk.ac.ceh.gateway.catalogue.ukeof;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @Template(called="html/ukeof.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class UKEOFDocument implements MetadataDocument {
    private String description, title, id, type;
    private MetadataInfo metadata;
    
    @Override
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }

    @Override
    public List<String> getLocations() {
       return Collections.EMPTY_LIST;
    }
}
