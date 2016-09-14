package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LinkDocument extends AbstractMetadataDocument {
    private final String linkedDocumentId;
    private final MetadataDocument original;
    private final List<Keyword> additionalKeywords;

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
    
    public LinkDocument withMetadataDocument(MetadataDocument original) {
        return new LinkDocument(this.linkedDocumentId, original, this.additionalKeywords);
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
