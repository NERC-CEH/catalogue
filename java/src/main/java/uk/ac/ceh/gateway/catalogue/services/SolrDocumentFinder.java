package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.Lists;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.util.List;

@Slf4j
@ToString
public class SolrDocumentFinder<T extends MetadataDocument> {
  private final SolrClient solrClient;
  private final DocumentReader<T> documentReader;
  private final Class<T> clazz;

  public SolrDocumentFinder(SolrClient solrClient, Class<T> clazz) {
    this.solrClient = solrClient;
    this.clazz = clazz;
    this.documentReader = new DocumentReader<T>();
    log.info("Creating {}", this);
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
      QueryResponse qr = solrClient.query(solrQuery);
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