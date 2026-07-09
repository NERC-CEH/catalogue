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
    private String combinedGeometry;
    private List<Link> belongsToNetwork;
    private List<Link> usedBy;
    private List<Link> utilisedBy;
    private List<Link> supersedes;
    private List<Link> supersededBy;
    private List<Link> childFacility;
    private List<Link> parentFacility;
    private List<Link> related;

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
        this.setCombinedGeometry(jenaService.relationshipCombinedGeometriesWithOwner(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility", locationConfidential));
        this.setBelongsToNetwork(jenaService.relationships(uri, "http://purl.org/dc/terms/isPartOf"));
        this.setUsedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"));
        this.setUtilisedBy(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"));
        this.setSupersedes(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setSupersededBy(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setChildFacility(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility"));
        this.setParentFacility(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility"));

        var relationList = jenaService.relationships(uri, "http://purl.org/dc/terms/relation");
        relationList.addAll(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelated(relationList);
    }

    public String getCombinedGeometry() {
        return combinedGeometry == null ? "" : combinedGeometry;
    }

    public List<Link> getBelongsToNetwork() {
        return Optional.ofNullable(belongsToNetwork)
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

    public List<Link> getChildFacility() {
        return Optional.ofNullable(childFacility)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getParentFacility() {
        return Optional.ofNullable(parentFacility)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelated() {
        return Optional.ofNullable(related)
            .orElseGet(Collections::emptyList);
    }
}
