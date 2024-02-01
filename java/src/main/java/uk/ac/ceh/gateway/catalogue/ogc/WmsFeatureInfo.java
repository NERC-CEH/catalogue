package uk.ac.ceh.gateway.catalogue.ogc;

import java.util.Map;
import java.util.List;
import lombok.Data;

@Data
public class WmsFeatureInfo {
    private List<Layer> layers;

    @Data
    public static class Layer {
        private String name;
        private List<Feature> features;

        @Data
        public static class Feature {
            private Map<String, String> attributes;
        }
    }
}
