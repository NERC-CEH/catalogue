package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

@Data
@EqualsAndHashCode(callSuper = true)
public class LinkDocument extends AbstractMetadataDocument {
    private String linkedDocumentId;
    private MetadataDocument original;
    private List<Keyword> additionalKeywords;

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
