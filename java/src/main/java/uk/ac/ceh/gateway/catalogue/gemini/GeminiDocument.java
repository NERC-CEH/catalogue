package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_XML_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.RDF_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeSerializer;
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
    @Template(called="xml/gemini.xml.tpl",   whenRequestedAs=GEMINI_XML_VALUE),
    @Template(called="rdf/gemini.xml.tpl",   whenRequestedAs=RDF_XML_VALUE)
})
public class GeminiDocument implements MetadataDocument {
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private URI uri;
    private String id, title, description, otherCitationDetails, browseGraphicUrl, resourceStatus, lineage,
        metadataStandardName, metadataStandardVersion, supplementalInfo, parentIdentifier, revisionOfIdentifier;
    @JsonIgnore
    private String jsonString;
    private List<String> alternateTitles, spatialRepresentationTypes, datasetLanguages,
      securityConstraints;
    private Keyword resourceType;        
    private List<Keyword> topicCategories;
    private List<DistributionInfo> distributionFormats;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private List<ConformanceResult> conformanceResults;
    private List<SpatialResolution> spatialResolutions;
    @JsonIgnore
    private MetadataInfo metadata;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> metadataPointsOfContact;
    private List<ResponsibleParty> distributorContacts;
    private List<ResponsibleParty> responsibleParties;
    private List<TimePeriod> temporalExtents;
    private List<OnlineResource> onlineResources;
    private Link parent, revised, revisionOf;
    private Set<Link> documentLinks, children, composedOf, modelLinks, modelApplicationLinks;
    private List<ResourceIdentifier> resourceIdentifiers;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    @JsonIgnore
    private Citation citation;
    @JsonIgnore
    private boolean isDataciteMintable;
    @JsonIgnore
    private boolean isDatacitable;
    private DatasetReferenceDate datasetReferenceDate;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime metadataDate;
    private List<ResourceMaintenance> resourceMaintenance;
    private Service service;
    private List<ResourceConstraint> accessConstraints, useConstraints;
    
    @JsonIgnore
    public String getMetadataDateTime() {
        /* This method always returns the full datetime string (including the seconds). LocalDateTime.toString() will 
           not return the seconds if it is a date with time 00:00:00 */
        if (metadataDate != null) {
            return metadataDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            return "";
        }
    }
        
    @JsonProperty("citation")
    public Citation getCitation() {
        return citation;
    }
    
    @JsonIgnore
    public GeminiDocument setCitation(Citation citation) {
        this.citation = citation;
        return this;
    }
    
    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
            .map(Keyword::getValue)
            .orElse("");
    }
    
    public Set<Link> getAssociatedResources() {
        Set<Link> toReturn = new HashSet<>();
        if (children != null) {
            toReturn.addAll(children);
        }
        if (documentLinks != null) {
            toReturn.addAll(documentLinks);
        }
        if (parent != null) {
            toReturn.add(parent);
        }
        if (revisionOf != null) {
            toReturn.add(revisionOf);
        }
        return toReturn;
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
        return Optional.ofNullable(onlineResources)
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch((o)-> WMS_GET_CAPABILITIES == o.getType());
    }

    @Override
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }
    
    public List<String> getTopics() {
        return Optional.ofNullable(descriptiveKeywords)
            .orElse(Collections.emptyList())
            .stream()
            .flatMap(dk -> dk.getKeywords().stream())
            .filter(k -> k.getUri().startsWith(TOPIC_PROJECT_URL))
            .map(Keyword::getUri)
            .collect(Collectors.toList());
    }
    
    public List<String> getCoupledResources() {
        return Optional.ofNullable(service)
            .map(Service::getCoupledResources)
            .orElse(Collections.emptyList())
            .stream()
            .map(Service.CoupledResource::getIdentifier)
            .filter(cr -> !cr.isEmpty())
            .collect(Collectors.toList());
    }
    
    public List<ResponsibleParty> getResponsibleParties() {
        return Optional.ofNullable(responsibleParties)
            .orElse(Collections.emptyList());
    }
      
    @Override
    public void attachUri(URI uri) {
        setUri(uri);
    }
    
}