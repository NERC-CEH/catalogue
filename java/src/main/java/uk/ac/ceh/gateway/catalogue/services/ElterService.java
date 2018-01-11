package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;
import org.apache.solr.client.solrj.SolrServer;
import lombok.AllArgsConstructor;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.elter.ManufacturerDocument;
import uk.ac.ceh.gateway.catalogue.elter.SensorDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@AllArgsConstructor
public class ElterService {

    private final DocumentRepository documentRepository;
    private final SolrServer solrServer;

    public ManufacturerDocument getManufacturer(String guid) {
        try {
            val document = documentRepository.read(guid);
            return (ManufacturerDocument) document;
        } catch(DocumentRepositoryException err) {
            throw new RuntimeException(err);
        }
    }

    public SensorDocument getSensor(String guid) {
        try {
            val document = documentRepository.read(guid);
            return (SensorDocument) document;
        } catch(DocumentRepositoryException err) {
            throw new RuntimeException(err);
        }
    }

    public List<SensorDocument> getSensors (ManufacturerDocument manufacturer) {
        val finder = new SolrDocumentFinder<SensorDocument>(solrServer, documentRepository);
        return finder.find(String.format("documentType:%s", ELTER_SENSOR_DOCUMENT_SHORT));
    }

    public List<SensorDocument> getSensors (String manufacturer) {
        val sensors = getSensors(getManufacturer(manufacturer));
        sensors.removeIf(sensor -> !sensor.getManufacturer().equals(manufacturer));
        return sensors;
    }

    public List<ManufacturerDocument> getManufacturers () {
        val finder = new SolrDocumentFinder<ManufacturerDocument>(solrServer, documentRepository);
        return finder.find(String.format("documentType:%s", ELTER_MANUFACTURER_DOCUMENT_SHORT));
    }
}
