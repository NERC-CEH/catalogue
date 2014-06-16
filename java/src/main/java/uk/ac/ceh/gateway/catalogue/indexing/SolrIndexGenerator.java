package uk.ac.ceh.gateway.catalogue.indexing;

/**
 * The following interface should parse some bean and generate a bean which is
 * annotated with solrj annotations such as @Field
 * 
 * The resultant bean will be submitted to solr for indexing.
 * @author cjohn
 */
public interface SolrIndexGenerator<D> {
    Object generateIndex(D toIndex);
}
