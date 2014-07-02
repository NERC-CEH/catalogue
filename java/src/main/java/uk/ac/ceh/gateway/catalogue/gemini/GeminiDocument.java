package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.elements.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DownloadOrder;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResponsibleParty;

/**
 *
 * @author cjohn
 */
@Data
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/metadata.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiDocument {
    
    private String id, title, description, otherCitationDetails, browseGraphicUrl;
    private List<String> alternateTitles, topicCategories;
    private CodeListItem datasetLanguage, resourceType;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private DownloadOrder downloadOrder;
    private MetadataInfo metadata;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> responsibleParties;
}
