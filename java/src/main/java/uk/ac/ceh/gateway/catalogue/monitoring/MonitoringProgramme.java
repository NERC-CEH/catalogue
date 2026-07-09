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
    private String relCombinedGeometry;
    private List<Link> relUses;
    private List<Link> relSupersedes;
    private List<Link> relSupersededBy;
    private List<Link> relActivities;
    private List<Link> relChildProgramme;
    private List<Link> relParentProgramme;
    private List<Link> relRelated;

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
        this.setRelCombinedGeometry(jenaService.programmeCombinedGeometries(uri));
        this.setRelUses(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"));
        this.setRelSupersedes(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelSupersededBy(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelActivities(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/triggers"));
        this.setRelChildProgramme(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme"));
        this.setRelParentProgramme(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme"));

        var relationList = jenaService.relationships(uri, "http://purl.org/dc/terms/relation");
        relationList.addAll(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelRelated(relationList);
    }

    public String getRelCombinedGeometry() {
        return relCombinedGeometry == null ? "" : relCombinedGeometry;
    }

    public List<Link> getRelUses() {
        return Optional.ofNullable(relUses)
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

    public List<Link> getRelActivities() {
        return Optional.ofNullable(relActivities)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelChildProgramme() {
        return Optional.ofNullable(relChildProgramme)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelParentProgramme() {
        return Optional.ofNullable(relParentProgramme)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelRelated() {
        return Optional.ofNullable(relRelated)
            .orElseGet(Collections::emptyList);
    }
}
