package uk.ac.ceh.gateway.catalogue.model;

import lombok.Value;
import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ceh.components.vocab.Concept;

@Value
public class SolrVocabularyIndex {
    public SolrVocabularyIndex(Concept concept, String vocab, String type) {
        this.uri = concept.getUri();
        this.term = concept.getTerm();
        this.vocab = vocab;
        this.type = type;
    }

    @Field String uri;
    @Field String term;
    @Field String vocab;
    @Field String type;
}
