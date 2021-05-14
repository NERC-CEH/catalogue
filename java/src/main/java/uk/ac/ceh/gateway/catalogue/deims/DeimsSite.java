package uk.ac.ceh.gateway.catalogue.deims;

import lombok.Data;

@Data
class DeimsSite {
    private String title;
    private Id id;
    private String coordinates;
    private String changed;

    public DeimsSite() {}

    public DeimsSite(String title, String prefix, String suffix) {
        this.title = title;
        this.id = new Id(prefix, suffix);
    }

    public String getIdentifier() {
        return id.getSuffix();
    }

    public String getURL() {
        return this.getId().prefix + this.getId().suffix;
    }

    @Data
    static class Id {
        private String prefix;
        private String suffix;

        public Id() {}

        public Id(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }
    }
}
