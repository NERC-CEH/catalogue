package uk.ac.ceh.gateway.catalogue.deims;


import lombok.Value;

@Value
public class DeimsSolrIndex {
    String title;
    String id;
    String url;

    public DeimsSolrIndex(DeimsSite site) {
        this.title = site.getTitle();
        this.id = site.getIdentifier();
        this.url = site.getURL();
    }
}
