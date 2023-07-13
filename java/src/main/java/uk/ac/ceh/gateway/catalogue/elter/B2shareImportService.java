package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrIndex;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.imports.CatalogueImportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
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
    private final PublicationService publicationService;
    private final SolrClient solrClient;
    private final String B2shareApiRoot;

    // constructor
    @SneakyThrows
    public B2shareImportService(
            DocumentRepository documentRepository,
            PublicationService publicationService,
            SolrClient solrClient,
            @Value("${b2share.api}") String B2shareApiRoot
            ) {
        log.info("Creating");

        this.documentRepository = documentRepository;
        this.objectMapper = new ObjectMapper();
        this.publicationService = publicationService;
        this.solrClient = solrClient;
        this.B2shareApiRoot = B2shareApiRoot;
        }

    // methods start here
    @SneakyThrows
    private List<String> getRemoteRecordList() {
        log.debug("GET B2SHARE records at {}", B2shareApiRoot);

        // prep
        List<String> results = new ArrayList<>();
        String B2shareRecordsUrl = B2shareApiRoot + "?q=community:d952913c-451e-4b5c-817e-d578dc8a4469&size=10000000";

        JsonNode B2shareRecords = objectMapper.readTree(new URL(B2shareRecordsUrl));

        for (JsonNode node : B2shareRecords.path("hits").path("hits")){
            String doi = node.path("metadata").path("DOI").asText();
            if (!doi.equals("")){
                results.add(doi);
            }
        }

        return results;
    }
    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport(){
        // prep
        log.info("Running B2SHARE metadata import...");
        CatalogueUser importUser = new CatalogueUser().setUsername("B2SHARE metadata import").setEmail("info@eudat.eu");
        //Map<String, String> localRecordList = null;
        int totalRecords = 0;
        int newRecords = 0;
        int updatedRecords = 0;
        int skippedRecords = 0;

        // get remote records
        List<String> remoteRecordList = getRemoteRecordList();
        totalRecords = remoteRecordList.size();

        // ready to import
        for (String recordUrl : remoteRecordList){
            log.info("{}", recordUrl);
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
