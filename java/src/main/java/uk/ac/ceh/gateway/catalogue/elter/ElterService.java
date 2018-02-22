package uk.ac.ceh.gateway.catalogue.elter;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;
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

  public TemporalProcedureDocument getTemporalProcedure(String guid) {
    val documentReader = new DocumentReader<TemporalProcedureDocument>();
    return documentReader.read(guid, TemporalProcedureDocument.class);
  }

  public List<SensorDocument> getSensors() {
    val finder = new SolrDocumentFinder<SensorDocument>(solrServer, SensorDocument.class);
    return finder.find(String.format("documentType:\"%s\"", ELTER_SENSOR_DOCUMENT_SHORT));
  }

  public List<SensorDocument> getSensors(String manufacturer) {
    val finder = new SolrDocumentFinder<SensorDocument>(solrServer, SensorDocument.class);
    return finder.find(String.format(
        "documentType:\"%s\" AND manufacturer:\"%s\"", ELTER_SENSOR_DOCUMENT_SHORT, manufacturer));
  }

  public List<ManufacturerDocument> getManufacturers() {
    val finder = new SolrDocumentFinder<ManufacturerDocument>(solrServer, ManufacturerDocument.class);
    return finder.find(String.format("documentType:\"%s\"", ELTER_MANUFACTURER_DOCUMENT_SHORT));
  }

  public List<TemporalProcedureDocument> getTemporalProcedures() {
    val finder = new SolrDocumentFinder<TemporalProcedureDocument>(solrServer, TemporalProcedureDocument.class);
    return finder.find(String.format("documentType:\"%s\"", ELTER_TEMPORAL_PROCEDURE_DOCUMENT_SHORT));
  }
}
