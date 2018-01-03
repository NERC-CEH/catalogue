package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;

@Data
public class DatasetOffered {
    private String name;
        private String type;
        private String size;
        private String value;
        private String document;
        public int getDescriptionSize() {
            return value == null ? 1 : value.split("\r\n|\r|\n").length;
        }

        public String toString() {
            return String.format("*name*: _%s_\n*format*: _%s_\n*size*: _%s_\n*descrption*: _%s_", name, type, size, value);
        }
}

