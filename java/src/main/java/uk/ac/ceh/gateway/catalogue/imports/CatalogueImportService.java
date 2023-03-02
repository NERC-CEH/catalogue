package uk.ac.ceh.gateway.catalogue.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

import org.springframework.web.client.RestClientResponseException;

public interface CatalogueImportService {
    // list of record IDs/locations
    List<String> getRemoteRecordList();

    // list of record IDs/locations
    //List<String> getLocalRecordList();

    // I think these aren't needed with the SITES import
    // or List<String> getLocalRecordsToUpdate(); ??
    //List<String> getNewRecords(List<???> localRecordList, List<???> remoteRecordList);
    // could be merged into getNewRecords?
    //List<AbstractMetadataDocument> getLocalRecordsToUpdate(List<???> localRecordList, List<???> remoteRecordList);

    // TODO: generic return type
    // remote work
    JsonNode getFullRemoteRecord(String recordID) throws RestClientResponseException, IOException, JsonProcessingException;

    // TODO: generic return type
    // local record
    //ElterDocument getLocalRecord(String recordID);

    // will this just end up calling the controller?
    String createRecord(String recordID);

    // will this just end up calling the controller?
    String updateRecord(String recordID);

    // calls all the other methods, the one to schedule
    // possible to require the @Scheduled annotation?
    void runImport() throws RestClientResponseException, IOException, JsonProcessingException;
}
