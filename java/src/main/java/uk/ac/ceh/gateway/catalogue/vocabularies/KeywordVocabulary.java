package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.Data;

@Data
public class KeywordVocabulary {
    private String label;
    private KeywordVocabulary.Id id;
    private String uri;

    public KeywordVocabulary() {}

    public KeywordVocabulary(String label, String prefix, String suffix) {
        this.label = label;
        this.id = new KeywordVocabulary.Id(prefix, suffix);
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
