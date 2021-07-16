package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.Value;
import org.apache.solr.client.solrj.beans.Field;

@Value
public class Keyword {
    @Field String label;
    @Field String vocabId;
    @Field String url;

    public Keyword(String label, String vocabId, String url) {
        this.label = label;
        this.vocabId = vocabId;
        this.url = url;
    }
}
