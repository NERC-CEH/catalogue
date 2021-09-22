package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.apache.solr.client.solrj.SolrServerException;

import java.util.List;

public interface ServiceAgreementSearch {
    List<ServiceAgreementSolrIndex> query(String query) throws SolrServerException;
}
