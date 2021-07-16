package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.Value;
import org.apache.solr.client.solrj.beans.Field;

@Value
public class Keyword {
    @Field String label;
    @Field String vocabId;
    @Field String url;
    
}
