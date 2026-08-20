package uk.ac.ceh.gateway.catalogue.researchActivity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called = "html/researchactivity/researchactivity.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
    @Template(called = "html/researchactivity/researchactivity.ttl", whenRequestedAs = RDF_TTL_VALUE)
})
public class ResearchActivity extends AbstractMetadataDocument {

    private List<String> alternateNames;
    private List<ResponsibleParty> contributors;
    private List<Funding> funding;
    private List<OnlineResource> onlineResources;

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

    @JsonIgnore
    // De-duplicated list of contributor organisations with valid identifiers
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
     
    //@JsonIgnore
    public List<Funder> getFunders() {

        record FunderKey(String organisationName, String organisationIdentifier) {}

        return Optional.ofNullable(funding)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(f ->
                f.getFunderName() != null && !f.getFunderName().isBlank() &&
                f.getFunderIdentifier() != null && !f.getFunderIdentifier().isBlank()
            )
            .collect(Collectors.groupingBy(
                f -> new FunderKey(
                    f.getFunderName(),
                    f.getFunderIdentifier()
                )
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

    public void populateFromJenaService(JenaLookupService jenaService) {
        final String uri = this.getUri();
        var relationList = new ArrayList<>(jenaService.relationships(uri, "http://purl.org/dc/terms/relation"));
        relationList.addAll(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelRelation(relationList);

        this.setRelAll(jenaService.allRelatedRecords(uri));

        var relationOutputs = new ArrayList<>(jenaService.relationships(uri, "http://purl.org/cerif/frapo/hasOutput"));
        relationOutputs.addAll(jenaService.inverseRelationships(uri, "http://purl.org/cerif/frapo/isOutputOf"));
        this.setRelHasOutput(relationOutputs);
    }
}



