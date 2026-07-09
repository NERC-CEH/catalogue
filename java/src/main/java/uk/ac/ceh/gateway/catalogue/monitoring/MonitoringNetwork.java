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
    private String combinedGeometry;
    private List<Link> featureList;
    private List<Link> usedBy;
    private List<Link> utilisedBy;
    private List<Link> supersedes;
    private List<Link> supersededBy;
    private List<Link> childNetwork;
    private List<Link> parentNetwork;
    private List<Link> related;

    @Override
    public List<String> getWKTs() {
        return Stream.ofNullable(boundingBox).map(BoundingBox::getWkt).toList();
    }

    public void populateFromJenaService(JenaLookupService jenaService) {
        final String uri = this.getUri();
        this.setCombinedGeometry(jenaService.inverseRelationshipCombinedGeometries(uri, "http://purl.org/dc/terms/isPartOf"));
        this.setFeatureList(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/isPartOf"));
        this.setUsedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"));
        this.setUtilisedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"));
        this.setSupersedes(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setSupersededBy(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setChildNetwork(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork"));
        this.setParentNetwork(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork"));

        var relationList = jenaService.relationships(uri, "http://purl.org/dc/terms/relation");
        relationList.addAll(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelated(relationList);
    }

    public String getCombinedGeometry() {
        return combinedGeometry == null ? "" : combinedGeometry;
    }

    public List<Link> getFeatureList() {
        return Optional.ofNullable(featureList)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getUsedBy() {
        return Optional.ofNullable(usedBy)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getUtilisedBy() {
        return Optional.ofNullable(utilisedBy)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getSupersedes() {
        return Optional.ofNullable(supersedes)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getSupersededBy() {
        return Optional.ofNullable(supersededBy)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getChildNetwork() {
        return Optional.ofNullable(childNetwork)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getParentNetwork() {
        return Optional.ofNullable(parentNetwork)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelated() {
        return Optional.ofNullable(related)
            .orElseGet(Collections::emptyList);
    }
}

