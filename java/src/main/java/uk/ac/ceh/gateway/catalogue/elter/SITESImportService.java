package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.imports.CatalogueImportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@Profile("server:elter")
@Slf4j
@Service
@ToString
public class SITESImportService implements CatalogueImportService {
    // constructor prep
    private final DocumentBuilder documentBuilder;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final PublicationService publicationService;
    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String sitemapUrl;
    private final XPathExpression xPath;

    // constructor
    public SITESImportService(
            DocumentRepository documentRepository,
            PublicationService publicationService,
            @Qualifier("normal") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${sites.import.url}") String sitemapUrl
            ) throws ParserConfigurationException, XPathExpressionException {
        log.info("Creating");
        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.documentRepository = documentRepository;
        this.objectMapper = new ObjectMapper();
        this.publicationService = publicationService;
        this.restTemplate = restTemplate;
        this.sitemapUrl = sitemapUrl;
        this.solrClient = solrClient;
        this.xPath = XPathFactory.newInstance().newXPath().compile("/urlset/url/loc");
            }

    // methods start here
    @SneakyThrows
    private List<String> getRemoteRecordList() {
        log.debug("GET SITES sitemap at {}", sitemapUrl);

        // prep
        List<String> results = new ArrayList<>();

        Document xmlSitemap = documentBuilder.parse(sitemapUrl);
        NodeList recordList = (NodeList) xPath.evaluate(xmlSitemap, XPathConstants.NODESET);
        int numRecords = recordList.getLength();
        for (int i=0; i<numRecords; i++){
            results.add(recordList.item(i).getTextContent());
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
        query.setParam(CommonParams.Q, "importId:https\\://hdl.handle.net/11676.1/*");
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
            log.error("10000000 results were returned: update the code in SITESImportService.java; aborting import");
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
    private JsonNode getFullRemoteRecord(String remoteRecordId) {
        log.debug("GET record {}", remoteRecordId);

        // get record HTML as String
        // in the SITES sitemap, remoteRecordId is the URL to the record
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(
                remoteRecordId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
                );
        String recordAsString = response.getBody();

        // extract ld+json string from HTML
        //
        // Yes, this isn't a stable way to retrieve the content of a tag...
        // but the XML isn't well-formed so until they fix it this will have to do
        BufferedReader reader = new BufferedReader(new StringReader(recordAsString));
        StringBuilder builder = new StringBuilder();
        String line = null;
        boolean started = false;

        while((line=reader.readLine()) != null){
            if(line.contains("<script type=\"application/ld+json\">")){
                started = true;
                continue;
            }
            if(started){
                if(line.contains("</script>")){
                    break;
                }
                builder.append(line);
            }
        }
        String recordJSONString = builder.toString();

        // parse ld+json string and return
        return objectMapper.readTree(recordJSONString);
    }

    @SneakyThrows
    private String createRecord(String remoteRecordId, JsonNode parsedRecord, CatalogueUser user) {
        // create from JSON
        ElterDocument newRecord = new ElterDocument();
        newRecord.importSitesJson(parsedRecord);

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
    private void updateRecord(String localRecordId, String remoteRecordId, JsonNode parsedRecord, CatalogueUser user) {
        // create from JSON
        ElterDocument updatedRecord = new ElterDocument();
        updatedRecord.importSitesJson(parsedRecord);

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
        log.info("Running SITES metadata import...");
        CatalogueUser importUser = new CatalogueUser().setUsername("SITES metadata import").setEmail("info@fieldsites.se");
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
        for (String recordUrl : remoteRecordList){
            JsonNode parsedRecord = getFullRemoteRecord(recordUrl);

            if (parsedRecord.get("@type").asText().equals("Dataset")){
                String remoteRecordId = parsedRecord.get("identifier").asText();
                if (localRecordList.containsKey(remoteRecordId)) {
                    updateRecord(localRecordList.get(remoteRecordId), remoteRecordId,  parsedRecord, importUser);
                    updatedRecords++;
                }
                else {
                    String newId = createRecord(remoteRecordId, parsedRecord, importUser);
                    log.debug("New document ID is {}", newId);
                    newRecords++;
                }
            }
            else {
                log.debug("Skipping record {} as it is not of type \"Dataset\"", recordUrl);
                skippedRecords++;
            }
        }

        // finished, log summary
        log.info("Finished SITES metadata import!");
        log.info("{} created + {} updated + {} skipped = {} total ({} records in sitemap)",
                newRecords,
                updatedRecords,
                skippedRecords,
                newRecords + updatedRecords + skippedRecords,
                totalRecords
                );
    }
}
