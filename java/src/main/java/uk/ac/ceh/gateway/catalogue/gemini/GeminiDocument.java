package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDate;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.elements.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DownloadOrder;
import uk.ac.ceh.gateway.catalogue.gemini.elements.Link;
import uk.ac.ceh.gateway.catalogue.gemini.elements.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.elements.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.gemini.elements.SpatialReferenceSystem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.TimePeriod;

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
    
    private String id, title, description, otherCitationDetails, browseGraphicUrl, resourceStatus;
    private List<String> alternateTitles, topicCategories, coupleResources;
    private CodeListItem datasetLanguage, resourceType;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private DownloadOrder downloadOrder;
    private MetadataInfo metadata;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> responsibleParties;
    private List<TimePeriod> temporalExtent;
    private List<OnlineResource> onlineResources;
    private Set<Link> documentLinks;
    private Set<ResourceIdentifier> resourceIdentifiers;
    private SpatialReferenceSystem spatialReferenceSystem;
    private DatasetReferenceDate datasetReferenceDate;
    private LocalDate metadataDate;
}
