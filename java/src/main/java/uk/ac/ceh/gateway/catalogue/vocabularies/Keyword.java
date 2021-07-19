package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

@Data
@AllArgsConstructor
public class Keyword {
    @Field private String label;
    @Field private String vocabId;
    @Field private String url;

    public Keyword() {}
}
