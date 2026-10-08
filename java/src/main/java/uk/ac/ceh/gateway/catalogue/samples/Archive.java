package uk.ac.ceh.gateway.catalogue.samples;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;

import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.geometry.Geometry;
import uk.ac.ceh.gateway.catalogue.model.Note;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called = "html/samples/archive.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
    @Template(called = "html/samples/archive.ttl", whenRequestedAs = RDF_TTL_VALUE)
})
public class Archive extends AbstractMetadataDocument implements WellKnownText {

    private ArchiveLocation archiveLocation;
    private String restrictions;
    private LocalDate archiveDate, reviewDate;
    private List<ResponsibleParty> contactPoints = new ArrayList<>();
    private List<ResponsibleParty> contributors = new ArrayList<>();
    private List<Note> notes = new ArrayList<>();
    private Boolean containsPersonalData;
    private List<Sample> samples = new ArrayList<>();

    @Data
    public static class ArchiveLocation {
        private String archive, locale, shelf;
    }

    @Data
    public static class Sample {
        private String sampleID, sampleDescription;
        private Geometry sampleLocation;
        private LocalDate sampleDate;
        private List<Keyword> sampleStorage, sampleCondition;
        private List<SemanticThing> things;
        
        public @NonNull List<String> getWKTs() {
            List<String> toReturn = new ArrayList<>();

            if (sampleLocation != null) {
                val possibleWkt = sampleLocation.getWkt();
                possibleWkt.ifPresent(toReturn::add);
            }

            return toReturn;
        }
    }

    @Override
    public @NonNull List<String> getWKTs() {
        return samples.stream()
            .flatMap(sample -> sample.getWKTs().stream())
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