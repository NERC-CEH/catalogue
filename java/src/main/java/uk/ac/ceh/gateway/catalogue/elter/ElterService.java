package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_MANUFACTURER_DOCUMENT_SHORT;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.ELTER_SENSOR_DOCUMENT_SHORT;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;

import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.services.DocumentReader;
import uk.ac.ceh.gateway.catalogue.services.SolrDocumentFinder;

@AllArgsConstructor
public class ElterService {
  private final SolrServer solrServer;

  public ManufacturerDocument getManufacturer(String guid) {
    val documentReader = new DocumentReader<ManufacturerDocument>();
    return documentReader.read(guid, ManufacturerDocument.class);
  }

  public SensorDocument getSensor(String guid) {
    val documentReader = new DocumentReader<SensorDocument>();
    return documentReader.read(guid, SensorDocument.class);
  }

  public List<SensorDocument> getSensors() {
    val finder = new SolrDocumentFinder<SensorDocument>(solrServer, SensorDocument.class);
    return finder.find(String.format("documentType:%s", ELTER_SENSOR_DOCUMENT_SHORT));
  }

  public List<SensorDocument> getSensors(String manufacturer) {
    val finder = new SolrDocumentFinder<SensorDocument>(solrServer, SensorDocument.class);
    return finder.find(String.format(
        "documentType:%s AND manufacturer:%s", ELTER_SENSOR_DOCUMENT_SHORT, manufacturer));
  }

  public List<ManufacturerDocument> getManufacturers() {
    val finder =
        new SolrDocumentFinder<ManufacturerDocument>(solrServer, ManufacturerDocument.class);
    return finder.find(String.format("documentType:%s", ELTER_MANUFACTURER_DOCUMENT_SHORT));
  }
}
