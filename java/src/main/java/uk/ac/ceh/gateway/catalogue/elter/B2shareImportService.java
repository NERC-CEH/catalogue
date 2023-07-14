package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.imports.CatalogueImportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@Profile("server:elter & imports")
@Slf4j
@Service
@ToString
public class B2shareImportService implements CatalogueImportService {
    // constructor prep
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final Pattern p;
    private final PublicationService publicationService;
    private final SolrClient solrClient;
    private final String b2shareRecordsUrl;
    private final String dataciteApiRoot;

    // constructor
    @SneakyThrows
    public B2shareImportService(
            DocumentRepository documentRepository,
            PublicationService publicationService,
            SolrClient solrClient,
            @Value("${b2share.api}") String b2shareRecordsUrl,
            @Value("${doi.api}") String dataciteApiRoot
            ) {
        log.info("Creating");

        this.documentRepository = documentRepository;
        this.objectMapper = new ObjectMapper();
        this.p = Pattern.compile("10\\.\\S+/\\S+");
        this.publicationService = publicationService;
        this.solrClient = solrClient;
        this.b2shareRecordsUrl = b2shareRecordsUrl;
        this.dataciteApiRoot = dataciteApiRoot;
        }

    // methods start here
    @SneakyThrows
    private List<String> getRemoteRecordList() {
        log.debug("GET B2SHARE records at {}", b2shareRecordsUrl);

        // prep
        List<String> results = new ArrayList<>();

        JsonNode b2shareRecords = objectMapper.readTree(new URL(b2shareRecordsUrl));

        for (JsonNode node : b2shareRecords.path("hits").path("hits")){
            String doi = node.path("metadata").path("DOI").asText();
            if (!doi.equals("")){
                results.add(doi);
            }
        }

        return results;
    }

    @SneakyThrows
    private Map<String, String> getLocalRecordMapping() throws IOException {
        log.debug("GET locally imported records");

        // prep
        Map<String, String> resultMapping = new HashMap<>(5000);

        // form and make SOLR query
        // could potentially reimplement with MetadataListingService.getPublicDocumentsOfCatalogue
        SolrQuery query = new SolrQuery();
        query.setParam(CommonParams.Q, "importId:10.23728/b2share.*");
        query.setParam(CommonParams.FL, "importId,identifier");
        // Ugh, there doesn't seem to be a way to return all results. To avoid
        // dealing with pagination just abort if 10000000 results are returned,
        // since we won't have checked all the records.
        //
        // At time of writing there are 881 records in the sitemap, so this
        // should basically never happen before the heat death of the universe.
        query.setRows(10000000);
        SolrDocumentList resultList = solrClient.query("documents", query, POST).getResults();

        // raise warning and abort, as promised above
        if (resultList.getNumFound() >= 10000000L){
            log.error("10000000 results were returned: update the code in B2shareImportService.java; aborting import");
            throw new IOException();
        }

        // populate mapping
        for (SolrDocument document : resultList){
            resultMapping.put(
                    (String) document.getFieldValue("importId"),
                    (String) document.getFieldValue("identifier")
                    );
        }

        return resultMapping;
    }

    @SneakyThrows
    private ElterDocument getFullRemoteRecord(String inputDoi) {
        String recordUrl = dataciteApiRoot + "/" + inputDoi;
        JsonNode dataciteJson = null;

        log.info("GET {}", recordUrl);
        try {
            dataciteJson = objectMapper.readTree(new URL(recordUrl));
        } catch (FileNotFoundException e) {
            return null;
        }

        ElterDocument document = new ElterDocument();
        document.importDataciteJson(dataciteJson);

        return document;
    }

    @SneakyThrows
    private String createRecord(String remoteRecordId, ElterDocument newRecord, CatalogueUser user) {
        // save document
        MetadataDocument savedDocument = documentRepository.saveNew(
                user,
                newRecord,
                "elter",
                "Create new record " + remoteRecordId
                );

        // publish new record
        publicationService.transition(user, savedDocument.getId(), "ykhm7b");
        publicationService.transition(user, savedDocument.getId(), "re4vkb");

        // success
        log.debug("Successfully imported record {}", remoteRecordId);
        return savedDocument.getId();
    }

    @SneakyThrows
    private void updateRecord(String localRecordId, String remoteRecordId, ElterDocument updatedRecord, CatalogueUser user) {
        // save back
        updatedRecord.setMetadata(documentRepository.read(localRecordId).getMetadata());
        documentRepository.save(
                user,
                updatedRecord,
                localRecordId,
                "Updated record " + remoteRecordId
                );

        // success
        log.debug("Successfully updated record {}", remoteRecordId);
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport(){
        // prep
        log.info("Running B2SHARE metadata import...");
        CatalogueUser importUser = new CatalogueUser().setUsername("B2SHARE metadata import").setEmail("info@eudat.eu");
        Map<String, String> localRecordList = null;
        int totalRecords = 0;
        int newRecords = 0;
        int updatedRecords = 0;
        int skippedRecords = 0;

        // get remote records
        List<String> remoteRecordList = getRemoteRecordList();
        totalRecords = remoteRecordList.size();

        // get local records
        try {
            localRecordList = getLocalRecordMapping();
        } catch (IOException ex) {
            log.error("Error retrieving locally imported records; aborting import");
            return;
        }

        // ready to import
        for (String recordDoiOrgUrl : remoteRecordList){
            // normalise input to DOI
            Matcher doiCheck = p.matcher(recordDoiOrgUrl);
            if (!doiCheck.find()) {
                log.info("No DOI detected in record {}", recordDoiOrgUrl);
                skippedRecords++;
                continue;
            }

            // try resolving DOI with datacite
            String recordDoi = doiCheck.group(0);
            ElterDocument remoteRecord = getFullRemoteRecord(recordDoi);
            if (remoteRecord == null){
                log.info("DOI {} does not exist", recordDoi);
                skippedRecords++;
                continue;
            }

            // ready to import
            remoteRecord.setImportId(recordDoi);
            remoteRecord.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));
            if (localRecordList.containsKey(recordDoi)) {
                updateRecord(localRecordList.get(recordDoi), recordDoi,  remoteRecord, importUser);
                updatedRecords++;
            }
            else {
                String newId = createRecord(recordDoi, remoteRecord, importUser);
                log.debug("New document ID is {}", newId);
                newRecords++;
            }
        }

        // finished, log summary
        log.info("Finished B2SHARE metadata import!");
        log.info("{} created + {} updated + {} skipped = {} total ({} records in B2SHARE)",
                newRecords,
                updatedRecords,
                skippedRecords,
                newRecords + updatedRecords + skippedRecords,
                totalRecords
                );
    }
}
