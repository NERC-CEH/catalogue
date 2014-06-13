package uk.ac.ceh.gateway.catalogue.gemini;

import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;

/**
 *
 * @author cjohn
 */
@Data
@ConvertUsing({
    @Template(called="html/metadata.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiDocument {
    
    private String id, title, description;
    private List<String> alternateTitles, topicCategories;
    private CodeListItem datasetLanguage;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private MetadataInfo metadata;
}
