package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.solr.client.solrj.beans.Field;

/**
 * The following class represents the parts of a sparql harvest which should be
 * indexed into solr
 * @author CJOHN
 */
@Data
@Accessors(chain=true)
public class SolrVocabularyIndex {
    private @Field String uri;
    private @Field String term;
    private @Field String vocab;
    private @Field String type;
}
