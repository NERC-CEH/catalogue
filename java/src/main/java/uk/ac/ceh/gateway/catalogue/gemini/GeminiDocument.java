package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;
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
    @Template(called="datacite/datacite.xml.tpl", whenRequestedAs="application/x-datacite+xml")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiDocument implements MetadataDocument {
    private URI uri;
    private String id, title, description, browseGraphicUrl, resourceStatus, lineage,
        metadataStandardName, metadataStandardVersion, supplementalInfo, resourceType;
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
    private Set<Link> documentLinks;
    private Set<ResourceIdentifier> resourceIdentifiers;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private Citation citation;
    private DatasetReferenceDate datasetReferenceDate;
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
    public void attachUri(URI uri) {
        setUri(uri);
    }
}
