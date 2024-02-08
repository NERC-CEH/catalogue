package uk.ac.ceh.gateway.catalogue.osdp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

import java.util.ArrayList;
import java.util.List;

//TODO this should be deprecate and removed as OSDP no longer used
@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/osdp/monitoringFacility.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class MonitoringFacility extends AbstractMetadataDocument implements WellKnownText {
    private String facilityType, geometry;
    private TimePeriod temporalExtent;
    private BoundingBox boundingBox;
    private List<ObservationCapability> observationCapabilities;

    @Override
    public List<String> getWKTs() {
        List<String> toReturn = new ArrayList<>();
        if(geometry != null) {
            toReturn.add(geometry);
        }
        if(boundingBox != null) {
            toReturn.add(boundingBox.getWkt());
        }
        return toReturn;
    }
}
