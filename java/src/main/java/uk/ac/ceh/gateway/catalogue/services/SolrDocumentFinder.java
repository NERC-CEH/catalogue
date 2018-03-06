package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import lombok.val;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

public class SolrDocumentFinder<T extends MetadataDocument> {
  private final SolrServer solrServer;
  private final DocumentReader<T> documentReader;
  private final Class<T> clazz;

  public SolrDocumentFinder(SolrServer solrServer, Class<T> clazz) {
    this.solrServer = solrServer;
    this.clazz = clazz;
    this.documentReader = new DocumentReader<T>();
  }

  public List<T> find(String query) {
    return find(query, 0, 500);
  }

  public List<T> find(String query, int start, int rows) {
    List<T> found = Lists.newArrayList();
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setRows(rows);
    solrQuery.setStart(start);
    solrQuery.setQuery(query);
    try {
      QueryResponse qr = solrServer.query(solrQuery);
      val solrDocumentList = qr.getResults();
      for (val solrDocument : solrDocumentList) {
        String guid = (String) solrDocument.getFieldValue("identifier");
        val document = documentReader.read(guid, clazz);
        found.add(document);
      }
    } catch (Exception err) {
      throw new RuntimeException(err);
    }
    return found;
  }
}