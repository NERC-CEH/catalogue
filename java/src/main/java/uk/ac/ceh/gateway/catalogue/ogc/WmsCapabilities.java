package uk.ac.ceh.gateway.catalogue.ogc;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WmsCapabilities implements Serializable {
    static final long serialVersionUID = 42L;
    private List<Layer> layers;
    private String directMap, directFeatureInfo;
}
