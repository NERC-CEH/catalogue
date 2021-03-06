package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ConvertUsing({
    @Template(called="html/link.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class LinkDocument extends AbstractMetadataDocument {
    private String linkedDocumentId;
    @JsonIgnore
    private MetadataDocument original;
    private List<Keyword> additionalKeywords;

    @JsonCreator
    @Builder
    private LinkDocument(
        @JsonProperty("linkedDocumentId") @NonNull String linkedDocumentId,
        @JsonProperty("original") MetadataDocument original,
        @JsonProperty("additionalKeywords") List<Keyword> additionalKeywords
    ) {
        this.linkedDocumentId = linkedDocumentId;
        this.original = original;
        this.additionalKeywords = additionalKeywords;
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LinkDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
