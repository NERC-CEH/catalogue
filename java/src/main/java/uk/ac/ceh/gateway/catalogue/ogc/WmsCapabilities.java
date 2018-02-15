package uk.ac.ceh.gateway.catalogue.ogc;

import java.util.List;
import lombok.Data;

@Data
public class WmsCapabilities {
    private List<Layer> layers;
    private String directMap, directFeatureInfo;
}
