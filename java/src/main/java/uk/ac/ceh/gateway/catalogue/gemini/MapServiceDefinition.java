package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.List;
import lombok.Data;

/**
 * Defines the details of a map server service definition. This object structure
 * closely represents the settings which can be made in a MapFile
 * @see http://mapserver.org/mapfile/index.html
 * @author cjohn
 */
@Data
public class MapServiceDefinition {
    private List<Layer> layers;
    
    @Data
    public static class Layer {
        private String epsgCode;
        private String type;
        private String name;
        private String data;
        private List<Class> classes;
        
        @Data
        public static class Class {
            private String name;
            private String expression;
            private Style style;
            
            @Data
            public static class Style {
                private String colour;
            }
        }
    }
}
