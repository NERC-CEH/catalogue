package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

@Data
public class KeywordSolrIndex {
    @Field private String label;
    @Field private String id;
    @Field private String url;

    public KeywordSolrIndex() {}

    public KeywordSolrIndex(KeywordVocabulary vocabulary) {
        this.label = vocabulary.getLabel();
        this.id = vocabulary.getIdentifier();
        this.url = vocabulary.getURL();
    }
}
