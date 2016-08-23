package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/linked.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class LinkDocument extends AbstractMetadataDocument {
    private String linkedDocumentId;
    private MetadataDocument original;
    private List<Keyword> additionalKeywords;
    
    @Override
    public String getTitle() {
        if (super.getTitle() != null) {
            return super.getTitle();
        } else if (original != null ) {
            return original.getTitle();
        } else {
            return "";
        }
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addAdditionalKeywords(List<Keyword> additionalKeywords) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
