package uk.ac.ceh.gateway.catalogue.gemini;

import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Link;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ceh.gateway.catalogue.model.Citation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import static uk.ac.ceh.gateway.catalogue.gemini.OnlineResource.Type.WMS_GET_CAPABILITIES;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_XML_VALUE;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.RDF_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

/**
 *
 * @author cjohn
 */
@Data 
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/gemini.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/gemini.xml.tpl",   whenRequestedAs=GEMINI_XML_VALUE),
    @Template(called="rdf/gemini.xml.tpl",   whenRequestedAs=RDF_XML_VALUE)
})
public class GeminiDocument extends AbstractMetadataDocument {
    private static final String TOPIC_PROJECT_URL = "http://onto.nerc.ac.uk/CEHMD/";
    private String otherCitationDetails, browseGraphicUrl, resourceStatus, lineage,
        metadataStandardName, metadataStandardVersion, parentIdentifier, revisionOfIdentifier;
    private List<String> alternateTitles, spatialRepresentationTypes, datasetLanguages,
      securityConstraints;      
    private List<Keyword> topicCategories;
    private List<DistributionInfo> distributionFormats;
    private List<DescriptiveKeywords> descriptiveKeywords;
    private List<ConformanceResult> conformanceResults;
    private List<SpatialResolution> spatialResolutions;
    private List<Funding> funding;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> metadataPointsOfContact;
    private List<ResponsibleParty> distributorContacts;
    private List<ResponsibleParty> responsibleParties;
    private List<TimePeriod> temporalExtents;
    private List<OnlineResource> onlineResources;
    private Link parent, revised, revisionOf;
    private Set<Link> documentLinks, children, composedOf, modelLinks, modelApplicationLinks;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private List<Supplemental> supplemental;
    @JsonIgnore
    private Citation citation;
    @JsonIgnore
    private boolean isDataciteMintable;
    @JsonIgnore
    private boolean isDatacitable;
    private DatasetReferenceDate datasetReferenceDate;
    private List<ResourceMaintenance> resourceMaintenance;
    private Service service;
    private List<ResourceConstraint> accessConstraints, useConstraints;
    private MapDataDefinition mapDataDefinition;
    private Keyword resourceType;
    
    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
            .map(Keyword::getValue)
            .orElse("");
    }
    
    @Override
    public GeminiDocument setType(String type) {
        super.setType(type);
        this.resourceType = Keyword.builder().value(type).build();
        return this;
    }
    
    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return Optional.ofNullable(descriptiveKeywords)
            .orElse(Collections.emptyList())
            .stream()
            .flatMap(dk -> dk.getKeywords().stream())
            .collect(Collectors.toList());
    }
    
    @Override
    public GeminiDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        descriptiveKeywords = Optional.ofNullable(descriptiveKeywords)
            .orElse(new ArrayList<>());
        
        descriptiveKeywords.add(
            DescriptiveKeywords
                .builder()
                .keywords(additionalKeywords)
                .build()
        );
        return this;
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
    
}