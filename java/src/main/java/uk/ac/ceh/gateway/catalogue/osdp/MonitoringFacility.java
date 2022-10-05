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
    @Template(called="html/osdp/monitoringFacility.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class MonitoringFacility extends AbstractMetadataDocument implements GeoJson {
    private String facilityType, geometry;
    private TimePeriod temporalExtent;
    private BoundingBox boundingBox;
    private List<ObservationCapability> observationCapabilities;

    @Override
    public List<String> getGeoJson() {
        List<String> toReturn = new ArrayList<>();
        if(geometry != null) {
            toReturn.add(geometry);
        }
        if(boundingBox != null) {
            toReturn.add(boundingBox.getGeoJson());
        }
        return toReturn;
    }
}
