package uk.ac.ceh.gateway.catalogue.deims;

import lombok.Data;

@Data
class DeimsSite {
    String title;
    Id id;
    String coordinates;
    String changed;

    public String getIdentifier() {
        return id.getSuffix();
    }

    public String getURL() {
        return this.getId().prefix + this.getId().suffix;
    }

    @Data
    private static class Id {

        String prefix;
        String suffix;
    }
}
