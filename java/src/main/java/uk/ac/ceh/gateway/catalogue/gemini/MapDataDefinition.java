package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
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
    public static class DataSource {
        private String type;
        private String path;
        private String layer;
        private String epsgCode;
        private List<Attribute> attributes;

        @Data
        public static class Attribute {
            private String name;
            private String id;

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
                private Number min;
                private Number max;
            }
        }
    }
    
    @Data
    public static class Style {
        private String colour;
    }
}
