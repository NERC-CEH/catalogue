package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/dataset.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class Dataset extends ResearchArtifact implements GeoJson {
    private List<Parameter> parametersMeasured;
    private String format, version;
    private BoundingBox boundingBox;

    @Override
    public List<String> getGeoJson() {
        List<String> toReturn = new ArrayList<>();
        if (boundingBox != null) {
            toReturn.add(boundingBox.getGeoJson());
        }
        return toReturn;
    }
}
