package uk.ac.ceh.gateway.catalogue.imports;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

public interface CatalogueImportService {
    // TODO: generic/new exceptions instead of IOExceptions everywhere?
    // TODO: generic return types? e.g. using AbstractMetadataDocuments for compatibility

    // returns list of record IDs/URLs
    List<String> getRemoteRecordList() throws IOException;

    // returns record as a parsed JsonNode
    JsonNode getFullRemoteRecord(String recordID) throws IOException;

    // returns local ID of new record
    String createRecord(String recordID, JsonNode parsedRecord, CatalogueUser importUser) throws DocumentRepositoryException;

    //void updateRecord(String recordID, JsonNode parsedRecord, CatalogueUser importUser) throws DocumentRepositoryException;

    // calls other record processing methods
    //void processRecord(String recordID);

    // calls all the other methods, the one to schedule
    // TODO: possible to require the @Scheduled annotation?
    void runImport();
}
