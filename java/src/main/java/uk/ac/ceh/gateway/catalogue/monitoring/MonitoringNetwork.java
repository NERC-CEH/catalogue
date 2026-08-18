package uk.ac.ceh.gateway.catalogue.monitoring;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/network.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/network.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringNetwork extends AbstractMetadataDocument implements WellKnownText {
    private List<String> alternateTitles;
    private String objectives, operationalStatus;
    @JsonAlias("pointsOfContact")
    private List<ResponsibleParty> contacts;
    private List<ResponsibleParty> partners;
    private TimePeriod operatingPeriod;
    private List<Keyword> environmentalDomain, keywordsParameters;
    private List<Supplemental> linksData, linksOther;
    private BoundingBox boundingBox;
    private String relCombinedGeometry;
    @JsonIgnore
    private List<Link> relFeatureList;
    @JsonIgnore
    private List<Link> relUsedBy;
    @JsonIgnore
    private List<Link> relUtilisedBy;
    @JsonIgnore
    private List<Link> relSupersedes;
    @JsonIgnore
    private List<Link> relSupersededBy;
    @JsonIgnore
    private List<Link> relChildNetwork;
    @JsonIgnore
    private List<Link> relParentNetwork;
    @JsonIgnore
    private List<Link> relRelated;

    @Override
    public List<String> getWKTs() {
        return Stream.ofNullable(boundingBox).map(BoundingBox::getWkt).toList();
    }

    public void populateFromJenaService(JenaLookupService jenaService) {
        final String uri = this.getUri();
        this.setRelCombinedGeometry(jenaService.inverseRelationshipCombinedGeometries(uri, "http://purl.org/dc/terms/isPartOf"));
        this.setRelFeatureList(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/isPartOf"));
        this.setRelUsedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"));
        this.setRelUtilisedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"));
        this.setRelSupersedes(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelSupersededBy(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelChildNetwork(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork"));
        this.setRelParentNetwork(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork"));

        var relationList = new ArrayList<>(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        relationList.addAll(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelRelated(relationList);
    }

    public String getRelCombinedGeometry() {
        return relCombinedGeometry == null ? "" : relCombinedGeometry;
    }

    @JsonProperty("relFeatureList")
    public List<Link> getRelFeatureList() {
        return Optional.ofNullable(relFeatureList)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelFeatureList(List<Link> relFeatureList) {
        this.relFeatureList = relFeatureList;
    }

    @JsonProperty("relUsedBy")
    public List<Link> getRelUsedBy() {
        return Optional.ofNullable(relUsedBy)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelUsedBy(List<Link> relUsedBy) {
        this.relUsedBy = relUsedBy;
    }

    @JsonProperty("relUtilisedBy")
    public List<Link> getRelUtilisedBy() {
        return Optional.ofNullable(relUtilisedBy)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelUtilisedBy(List<Link> relUtilisedBy) {
        this.relUtilisedBy = relUtilisedBy;
    }

    @JsonProperty("relSupersedes")
    public List<Link> getRelSupersedes() {
        return Optional.ofNullable(relSupersedes)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelSupersedes(List<Link> relSupersedes) {
        this.relSupersedes = relSupersedes;
    }

    @JsonProperty("relSupersededBy")
    public List<Link> getRelSupersededBy() {
        return Optional.ofNullable(relSupersededBy)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelSupersededBy(List<Link> relSupersededBy) {
        this.relSupersededBy = relSupersededBy;
    }

    @JsonProperty("relChildNetwork")
    public List<Link> getRelChildNetwork() {
        return Optional.ofNullable(relChildNetwork)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelChildNetwork(List<Link> relChildNetwork) {
        this.relChildNetwork = relChildNetwork;
    }

    @JsonProperty("relParentNetwork")
    public List<Link> getRelParentNetwork() {
        return Optional.ofNullable(relParentNetwork)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelParentNetwork(List<Link> relParentNetwork) {
        this.relParentNetwork = relParentNetwork;
    }

    @JsonProperty("relRelated")
    public List<Link> getRelRelated() {
        return Optional.ofNullable(relRelated)
            .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public void setRelRelated(List<Link> relRelated) {
        this.relRelated = relRelated;
    }
}

