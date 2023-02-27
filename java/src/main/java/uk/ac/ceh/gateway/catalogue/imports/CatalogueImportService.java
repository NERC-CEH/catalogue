package uk.ac.ceh.gateway.catalogue.imports;

import java.util.List;

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

    // remote work
    String getFullRemoteRecord(String recordID);

    // will this just end up calling the controller?
    String createRecord(String recordID);

    // will this just end up calling the controller?
    String updateRecord(String recordID);

    // calls all the other methods, the one to schedule
    // possible to require the @Scheduled annotation?
    void runImport();
}
