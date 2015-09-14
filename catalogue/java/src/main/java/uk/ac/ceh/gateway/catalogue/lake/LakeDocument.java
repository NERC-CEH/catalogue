package uk.ac.ceh.gateway.catalogue.lake;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import lombok.Data;
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
@ConvertUsing({
    @Template(called="html/lake.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class LakeDocument implements MetadataDocument {
    private URI uri;
    private String title, id, geometry;
    
    @Override
    public String getDescription() {
        return title;
    }
        
    @Override
    public String getType() {
        return "lake";
    }
    
    @JsonIgnore
    private MetadataInfo metadata;
    
    @Override
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }

    @Override
    public void attachUri(URI uri) {
        setUri(uri);
    }
    
}
