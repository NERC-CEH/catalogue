package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

@Data
public class VocabularySolrIndex {
    @Field private String label;
    @Field private String VocabId;
    @Field private String url;

    public VocabularySolrIndex(String label, String vocabId, String url) {
        this.label = label;
        VocabId = vocabId;
        this.url = url;
    }
}
