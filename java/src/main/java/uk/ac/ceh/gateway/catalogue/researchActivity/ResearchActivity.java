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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @JsonIgnore
    //De-duplicated list of contributor and funder organisations with valid identifiers
    public List<Organisation> getOrganisations() {
        return Stream.concat(
                Optional.ofNullable(funding)
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(f -> new Organisation(
                        f.getFunderName(),
                        f.getFunderIdentifier()
                    )),
                Optional.ofNullable(contributors)
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(c -> new Organisation(
                        c.getOrganisationName(),
                        c.getOrganisationIdentifier()
                    ))
            )
            .filter(o -> 
                (o.getOrganisationName() != null && !o.getOrganisationName().isBlank()) &&
                (o.getOrganisationIdentifier() != null && !o.getOrganisationIdentifier().isBlank())
                )
            .distinct()
            .toList();
    }

    public void populateFromJenaService(JenaLookupService jenaService) {
        final String uri = this.getUri();
        var relationList = jenaService.relationships(uri, "http://purl.org/dc/terms/relation");
        relationList.addAll(jenaService.inverseRelationships(uri, "http://purl.org/dc/terms/relation"));
        this.setRelRelation(relationList);

        this.setRelAll(jenaService.allRelatedRecords(uri));

        var relationOutputs = jenaService.relationships(uri, "http://purl.org/cerif/frapo/hasOutput");
        relationOutputs.addAll(jenaService.inverseRelationships(uri, "http://purl.org/cerif/frapo/isOutputOf"));
        this.setRelHasOutput(relationOutputs);
    }
}
