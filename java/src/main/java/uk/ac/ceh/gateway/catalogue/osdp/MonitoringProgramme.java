package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/monitoringProgramme.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class MonitoringProgramme extends AbstractMetadataDocument implements GeoJson {
    private TimePeriod temporalExtent;
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
