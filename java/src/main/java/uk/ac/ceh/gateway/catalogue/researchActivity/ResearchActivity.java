package uk.ac.ceh.gateway.catalogue.researchActivity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/researchActivity/researchActivity.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/researchActivity/researchActivity.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class ResearchActivity extends AbstractMetadataDocument implements WellKnownText {
    private List<String> alternateTitles;
    private String activityType, objectives, operationalStatus;
    private BoundingBox boundingBox;
    @JsonAlias("pointsOfContact")
    private List<ResponsibleParty> contactPoints = new ArrayList<>();
    private List<ResponsibleParty> contributors = new ArrayList<>();
    private List<ResponsibleParty> partners = new ArrayList<>();
    private List<TimePeriod> operatingPeriod;
    private List<Keyword> environmentalDomain, purposeOfCollection, keywordsParameters;
    private List<Supplemental> linksData, linksOther;

    private List<Funding> funding;

    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
    private String relCombinedGeometry;
    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
    private List<Link> relUses;
    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
    private List<Link> relSupersedes;
    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
    private List<Link> relSupersededBy;
    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
    private List<Link> relActivities;
    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
    private List<Link> relChildResearchActivity;
    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
    private List<Link> relParentResearchActivity;
    @JsonIgnore
    @Setter(onMethod_ = @JsonIgnore)
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
        this.setRelCombinedGeometry(jenaService.researchActivityCombinedGeometries(uri));
        this.setRelUses(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises"));
        this.setRelSupersedes(jenaService.relationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelSupersededBy(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/replaces"));
        this.setRelActivities(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/triggers"));
        this.setRelChildResearchActivity(jenaService.relationships(uri, "http://purl.org/dc/terms/hasPart"));
        this.setRelParentResearchActivity(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/hasPart"));

        var relationList = new ArrayList<>(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        relationList.addAll(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelRelated(relationList);
    }

    @JsonProperty("relCombinedGeometry")
    public String getRelCombinedGeometry() {
        return relCombinedGeometry == null ? "" : relCombinedGeometry;
    }

    @JsonProperty("relUses")
    public List<Link> getRelUses() {
        return Optional.ofNullable(relUses)
            .orElseGet(Collections::emptyList);
    }

    @JsonProperty("relSupersedes")
    public List<Link> getRelSupersedes() {
        return Optional.ofNullable(relSupersedes)
            .orElseGet(Collections::emptyList);
    }

    @JsonProperty("relSupersededBy")
    public List<Link> getRelSupersededBy() {
        return Optional.ofNullable(relSupersededBy)
            .orElseGet(Collections::emptyList);
    }

    @JsonProperty("relActivities")
    public List<Link> getRelActivities() {
        return Optional.ofNullable(relActivities)
            .orElseGet(Collections::emptyList);
    }

    @JsonProperty("relChildResearchActivity")
    public List<Link> getRelChildResearchActivity() {
        return Optional.ofNullable(relChildResearchActivity)
            .orElseGet(Collections::emptyList);
    }

    @JsonProperty("relParentResearchActivity")
    public List<Link> getRelParentResearchActivity() {
        return Optional.ofNullable(relParentResearchActivity)
            .orElseGet(Collections::emptyList);
    }

    @JsonProperty("relRelated")
    public List<Link> getRelRelated() {
        return Optional.ofNullable(relRelated)
            .orElseGet(Collections::emptyList);
    }

    @Data
    @AllArgsConstructor
    public static class Organisation {
        private String organisationName;
        private String organisationIdentifier;
    }
    
    @Data
    @AllArgsConstructor
    public static class Funder {
        private String organisationName;
        private String organisationIdentifier;
        private List<Award> awards;
    }

    @Data
    @AllArgsConstructor
    public static class Award {
        private String awardNumber;
        private String awardTitle;
        private String awardURI;
    }

    /** De-duplicated contributor organisations that carry an identifier. */
    @JsonIgnore
    public List<Organisation> getOrganisations() {
        return Optional.ofNullable(contributors)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(c -> new Organisation(
                c.getOrganisationName(),
                c.getOrganisationIdentifier()
            ))
            .filter(o ->
                o.getOrganisationName() != null &&
                !o.getOrganisationName().isBlank() &&
                o.getOrganisationIdentifier() != null &&
                !o.getOrganisationIdentifier().isBlank()
            )
            .distinct()
            .toList();
    }
    
    /**
     * Funding regrouped by funder, so a funder with several awards renders once.
     * Derived from {@code funding}, so it must not be serialised: a stored copy
     * would be a second source of truth that goes stale the moment funding is
     * edited.
     */
    @JsonIgnore
    public List<Funder> getFunders() {

        record FunderKey(String organisationName, String organisationIdentifier) {}

        return Optional.ofNullable(funding)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(f ->
                f.getFunderName() != null && !f.getFunderName().isBlank()
            )
            .collect(Collectors.groupingBy(
                f -> new FunderKey(
                    f.getFunderName(),
                    f.getFunderIdentifier()
                ),
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet()
            .stream()
            .map(e -> new Funder(
                e.getKey().organisationName(),
                e.getKey().organisationIdentifier(),
                e.getValue().stream()
                    .filter(f ->
                        Stream.of(
                            f.getAwardNumber(),
                            f.getAwardTitle(),
                            f.getAwardURI()
                        )
                        .filter(Objects::nonNull)
                        .anyMatch(s -> !s.isBlank())
                    )
                    .map(f -> new Award(
                        f.getAwardNumber(),
                        f.getAwardTitle(),
                        f.getAwardURI()
                    ))
                    .distinct()
                    .toList()
            ))
            .toList();
    }

        @JsonIgnore
    public List<ResponsibleParty> getContacts() {
        return Stream.of(
            Optional.ofNullable(contributors)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("contributor"))
                        .toList(),
            Optional.ofNullable(contactPoints)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("pointOfContact"))
                        .toList(),
            Optional.ofNullable(partners)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(contact -> contact.withRole("partner"))
                        .toList()
        )
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
