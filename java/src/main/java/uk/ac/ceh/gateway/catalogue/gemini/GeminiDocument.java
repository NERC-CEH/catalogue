package uk.ac.ceh.gateway.catalogue.gemini;

import uk.ac.ceh.gateway.catalogue.gemini.elements.DatasetLanguage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

/**
 *
 * @author cjohn
 */
@Data
@ConvertUsing({
    @Template(called="template", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiDocument {
    
    //Mandatory fields
    private String id, title, alternateTitle;
    
    //Conditional
    private DatasetLanguage datasetLanguage;
    
    //Ignored, it is the responsibility of a DocumentBundleService to bundle the 
    //MetadataInfo to an instance of Metadata
    @JsonIgnore
    private MetadataInfo metadata;
}
