package uk.ac.ceh.gateway.catalogue.deims;


import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

@Data
public class DeimsSolrIndex {
    @Field private String title;
    @Field private String id;
    @Field private String url;

    @SuppressWarnings("unused")
    private DeimsSolrIndex() {}

    public DeimsSolrIndex(DeimsSite site) {
        this.title = site.getTitle();
        this.id = site.getIdentifier();
        this.url = site.getURL();
    }
}
