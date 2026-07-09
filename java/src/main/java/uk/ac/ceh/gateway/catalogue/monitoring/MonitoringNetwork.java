package uk.ac.ceh.gateway.catalogue.monitoring;

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
    private List<ResponsibleParty> pointsOfContact, partners;
    private TimePeriod operatingPeriod;
    private List<Keyword> environmentalDomain, keywordsParameters;
    private List<Supplemental> linksData, linksOther;
    private BoundingBox boundingBox;
    private String relCombinedGeometry;
    private List<Link> relFeatureList;
    private List<Link> relUsedBy;
    private List<Link> relUtilisedBy;
    private List<Link> relSupersedes;
    private List<Link> relSupersededBy;
    private List<Link> relChildNetwork;
    private List<Link> relParentNetwork;
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

        var relationList = jenaService.relationships(uri, "http://purl.org/dc/terms/relation");
        relationList.addAll(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelRelated(relationList);
    }

    public String getRelCombinedGeometry() {
        return relCombinedGeometry == null ? "" : relCombinedGeometry;
    }

    public List<Link> getRelFeatureList() {
        return Optional.ofNullable(relFeatureList)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelUsedBy() {
        return Optional.ofNullable(relUsedBy)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelUtilisedBy() {
        return Optional.ofNullable(relUtilisedBy)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelSupersedes() {
        return Optional.ofNullable(relSupersedes)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelSupersededBy() {
        return Optional.ofNullable(relSupersededBy)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelChildNetwork() {
        return Optional.ofNullable(relChildNetwork)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelParentNetwork() {
        return Optional.ofNullable(relParentNetwork)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelRelated() {
        return Optional.ofNullable(relRelated)
            .orElseGet(Collections::emptyList);
    }
}

