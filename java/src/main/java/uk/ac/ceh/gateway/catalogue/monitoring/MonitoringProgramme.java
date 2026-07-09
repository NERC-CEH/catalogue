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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/programme.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/programme.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringProgramme extends AbstractMetadataDocument implements WellKnownText {
    private List<String> alternateTitles;
    private String objectives, operationalStatus;
    private BoundingBox boundingBox;
    private List<ResponsibleParty> pointsOfContact, partners;
    private List<TimePeriod> operatingPeriod;
    private List<Keyword> environmentalDomain, purposeOfCollection, keywordsParameters;
    private List<Supplemental> linksData, linksOther;
    private String combinedGeometry;
    private List<Link> uses;
    private List<Link> supersedes;
    private List<Link> supersededBy;
    private List<Link> activities;
    private List<Link> childProgramme;
    private List<Link> parentProgramme;
    private List<Link> related;

    @Override
    public List<String> getWKTs() {
        List<String> toReturn = new ArrayList<>();
        if (boundingBox != null) {
            toReturn.add(boundingBox.getWkt());
        }
        return toReturn;
    }

    public void populateFromJenaService(JenaLookupService jenaService) {
        final String uri = this.getUri();
        this.setCombinedGeometry(jenaService.programmeCombinedGeometries(uri));
        this.setUses(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"));
        this.setSupersedes(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setSupersededBy(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setActivities(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/triggers"));
        this.setChildProgramme(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme"));
        this.setParentProgramme(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme"));

        var relationList = jenaService.relationships(uri, "http://purl.org/dc/terms/relation");
        relationList.addAll(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelated(relationList);
    }

    public String getCombinedGeometry() {
        return combinedGeometry == null ? "" : combinedGeometry;
    }

    public List<Link> getUses() {
        return Optional.ofNullable(uses)
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

    public List<Link> getActivities() {
        return Optional.ofNullable(activities)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getChildProgramme() {
        return Optional.ofNullable(childProgramme)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getParentProgramme() {
        return Optional.ofNullable(parentProgramme)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelated() {
        return Optional.ofNullable(related)
            .orElseGet(Collections::emptyList);
    }
}
