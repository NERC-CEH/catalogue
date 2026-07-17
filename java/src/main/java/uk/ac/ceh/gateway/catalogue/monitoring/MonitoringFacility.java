package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.val;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/facility.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/facility.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringFacility extends AbstractMetadataDocument implements WellKnownText {
    private String operationalStatus;
    private List<String> alternateTitles;
    private Keyword facilityType;
    private Geometry geometry;
    private boolean geometryRepresentative, locationConfidential, mobile;
    private List<ResponsibleParty> pointsOfContact, partners;
    private List<TimePeriod> operatingPeriod;
    private List<Keyword> environmentalDomain, keywordsParameters;
    private List<AdditionalInfo> additionalInfo;
    private String relCombinedGeometry;
    private List<Link> relBelongsToNetwork;
    private List<Link> relUsedBy;
    private List<Link> relUtilisedBy;
    private List<Link> relSupersedes;
    private List<Link> relSupersededBy;
    private List<Link> relChildFacility;
    private List<Link> relParentFacility;
    private List<Link> relRelated;

    @Data
    public static class AdditionalInfo {
        private String
            key,
            value;
    }

    @Override
    public @NonNull List<String> getWKTs() {
        List<String> toReturn = new ArrayList<>();
        if(geometry != null) {
            val possibleWkt = geometry.getWkt();
            possibleWkt.ifPresent(toReturn::add);
        }
        return toReturn;
    }

    public void populateFromJenaService(JenaLookupService jenaService) {
        final String uri = this.getUri();
        this.setRelCombinedGeometry(jenaService.relationshipCombinedGeometriesWithOwner(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility", locationConfidential));
        this.setRelBelongsToNetwork(jenaService.relationships(uri, "http://purl.org/dc/terms/isPartOf"));
        this.setRelUsedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"));
        this.setRelUtilisedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"));
        this.setRelSupersedes(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelSupersededBy(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelChildFacility(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility"));
        this.setRelParentFacility(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility"));

        var relationList = new ArrayList<>(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        relationList.addAll(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelRelated(relationList);
    }

    public String getRelCombinedGeometry() {
        return relCombinedGeometry == null ? "" : relCombinedGeometry;
    }

    public List<Link> getRelBelongsToNetwork() {
        return Optional.ofNullable(relBelongsToNetwork)
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

    public List<Link> getRelChildFacility() {
        return Optional.ofNullable(relChildFacility)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelParentFacility() {
        return Optional.ofNullable(relParentFacility)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelRelated() {
        return Optional.ofNullable(relRelated)
            .orElseGet(Collections::emptyList);
    }
}
