package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.elter.ManufacturerDocument;
import uk.ac.ceh.gateway.catalogue.elter.SensorDocument;

@AllArgsConstructor
public class ElterService {

    private final SolrServer solrServer;

    public List<SensorDocument> getSensors (ManufacturerDocument manufacturer) {
        return null;
    }

    public List<ManufacturerDocument> getManufacturers () {
        return null;
    }
}