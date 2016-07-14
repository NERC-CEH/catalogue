package uk.ac.ceh.gateway.catalogue.gemini;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Defines the details of Spatial data (e.g. location, projection system) which
 * will then be used in the generation of MapServer MapFiles.
 * 
 * If a layer is specified within a DataSource, this indicates that ogr should
 * be used as the connection type.
 * @author cjohn
 */
@Data
public class MapDataDefinition {
    private List<DataSource> data;
    
    @Data
    public static class Projection {
        private String path;
        private String epsgCode;
    }
    
    @Data
    @EqualsAndHashCode(callSuper=true)
    public static class DataSource extends Projection {
        private String type;
        private String layer;
        private List<Projection> reprojections;
        private List<Attribute> attributes;
        
        public enum AttributeType {
            TEXT,NUMBER
        }

        @Data
        public static class Attribute {
            private String name;
            private String id;
            private String label;
            private AttributeType type;

            private List<Value> values;
            private List<Bucket> buckets;
            
            @Data
            public static class Value {
                private Style style;
                private String label;
                private String setting;
            }
            
            @Data
            public static class Bucket {
                private Style style;
                private String label;
                private BigDecimal min;
                private BigDecimal max;
            }
        }
    }
    
    @Data
    public static class Style {
        private String colour;
    }
}
