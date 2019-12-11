package uk.ac.ceh.gateway.catalogue.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The following spring JavaConfig defines the beans required for the interacting
 * with an external solr server whose url is defined as a property in the application
 * properties file
 */
@Configuration
public class HttpSolrClientConfig {
    @Value("${solr.server.documents.url}") String solrDocumentServerUrl;
    
    @Bean(name="documents")
    public SolrClient solrDocumentsServer(){
        return new HttpSolrClient.Builder(solrDocumentServerUrl).build();
    }
}
