package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/sample.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class Sample extends ResearchArtifact implements GeoJson {
    private String medium, geometry;

    @Override
    public List<String> getGeoJson() {
        List<String> toReturn = new ArrayList<>();
        if (geometry != null) {
            toReturn.add(geometry);
        }
        return toReturn;
    }
}
