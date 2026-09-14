package uk.ac.ceh.gateway.catalogue.samples;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
//import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Note;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called = "html/samples/sample.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
    @Template(called = "html/samples/sample.ttl", whenRequestedAs = RDF_TTL_VALUE)
})
public class Sample extends AbstractMetadataDocument {

    private StorageLocation storageLocation ;
    private String restrictions ;
    private final LocalDate archiveDate, reviewDate;
    
    private List<ResponsibleParty> contactPoints = new ArrayList<>();
    private List<ResponsibleParty> contributors = new ArrayList<>();
    private List<Note> notes = new ArrayList<>();
    private Boolean containsPersonalData;
    private List<Sample> subsamples;

    @Data
    public static class StorageLocation {
        private String archive, locale, shelf;
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
