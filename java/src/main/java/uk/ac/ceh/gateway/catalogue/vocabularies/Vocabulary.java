package uk.ac.ceh.gateway.catalogue.vocabularies;

import org.apache.solr.client.solrj.beans.Field;

public class Vocabulary {
    private String label;
    private String VocabId;
    private String url;

    public Vocabulary(String label, String vocabId, String url) {
        this.label = label;
        VocabId = vocabId;
        this.url = url;
    }

    public Vocabulary() {
    }
    public String getLabel() {
        return label;
    }

    public String getVocabId() {
        return VocabId;
    }

    public String getUrl() {
        return url;
    }

}
