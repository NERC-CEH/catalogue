package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.model.Association;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Accessors(chain=true)
@ConvertUsing({
    @Template(called="html/prov/prov-document.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class ProvDocument extends AbstractMetadataDocument {
    private List<RelatedRecord> relatedRecords;
    private List<Association> associations;

}
