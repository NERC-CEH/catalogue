                            package uk.ac.ceh.gateway.catalogue.datalabs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Accessors(chain=true)
@ConvertUsing({
    @Template(called="html/datalabs/datalabs-document.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class DatalabsDocument extends AbstractMetadataDocument {
    private String version, masterUrl;
    private List<ResponsibleParty> owners;
}
