package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

import java.util.List;

@Data
@ConvertUsing({
    @Template(called="html/osdp/location.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class Location {
    private BoundingBox boundingBox;
    private String geometry;
    private List<Keyword> keywords;
}
