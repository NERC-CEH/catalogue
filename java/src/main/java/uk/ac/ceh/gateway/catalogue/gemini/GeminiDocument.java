package uk.ac.ceh.gateway.catalogue.gemini;

import uk.ac.ceh.gateway.catalogue.model.Citation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.config.WebConfig;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateSerializer;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

/**
 *
 * @author cjohn
 */
@Data
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/gemini.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="datacite/datacite.xml.tpl", whenRequestedAs=WebConfig.DATACITE_XML_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true, value = {"parentIdentifier", "resourceType", "downloadOrder", "metadata", "locations",
"mapViewerUrl", "mapViewable", "topics"})
public class GeminiDocument implements MetadataDocument {
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private URI uri;
    private String id, title, description, otherCitationDetails, browseGraphicUrl, resourceStatus, lineage,
        metadataStandardName, metadataStandardVersion, supplementalInfo, resourceType, parentIdentifier;
    private List<String> alternateTitles, topicCategories, coupledResources, spatialRepresentationTypes, datasetLanguages,
        useLimitations, accessConstraints, otherConstraints, securityConstraints;
    private List<DistributionInfo> distributionFormats;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private List<ConformanceResult> conformanceResults;
    private List<SpatialResolution> spatialResolutions;
    private DownloadOrder downloadOrder;
    private MetadataInfo metadata;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> metadataPointsOfContact;
    private List<ResponsibleParty> distributorContacts;
    private List<ResponsibleParty> responsibleParties;
    private List<TimePeriod> temporalExtent;
    private List<OnlineResource> onlineResources;
    private Link parent;
    private Set<Link> documentLinks, children;
    private Set<ResourceIdentifier> resourceIdentifiers;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private Citation citation;
    private DatasetReferenceDate datasetReferenceDate;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate metadataDate;
    
    @Override
    public String getType() {
        return getResourceType();
    }
    
    @Override
    public List<String> getLocations() {
        return boundingBoxes
                .stream()
                .map(BoundingBox::getSolrGeometry)
                .collect(Collectors.toList());
    }
    
    /**
     * Return a link to the map viewer for this Gemini record if it can be
     * rendered in the map viewer
     * @return The link to the map viewer if it is viewable else null
     */
    public String getMapViewerUrl() {
        if(isMapViewable()) {
            return "/maps#layers/" + getId();
        }
        return null;
    }
    
    /**
     * Decide if this gemini document has a map viewing capability. That is at 
     * least one wms is registered to this gemini document
     * @return true if a wms exists in the online resources
     */
    public boolean isMapViewable() {
        return onlineResources
                .stream()
                .anyMatch((o)-> WMS_GET_CAPABILITIES == o.getType());
    }

    @Override
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }
    
    @Override
    public List<String> getTopics() {
        return descriptiveKeywords
            .stream()
            .flatMap(dk -> dk.getKeywords().stream())
            .filter(k -> k.getUri().startsWith(TOPIC_PROJECT_URL))
            .map(Keyword::getUri)
            .collect(Collectors.toList());
    }
    
    @Override
    public void attachUri(URI uri) {
        setUri(uri);
    }
}
