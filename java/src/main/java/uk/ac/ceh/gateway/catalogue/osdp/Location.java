package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/location.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class Location extends AbstractMetadataDocument {
    private BoundingBox boundingBox;
    private String geometry;
}
