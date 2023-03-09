package uk.ac.ceh.gateway.catalogue.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Profile("elter")
@Slf4j
@Service
@ToString
public class SITESImportService implements CatalogueImportService {
    // constructor prep
    private final DocumentBuilder documentBuilder;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final XPath xPath;

    // constructor
    public SITESImportService(
            DocumentRepository documentRepository,
            @Qualifier("normal") RestTemplate restTemplate,
            SolrClient solrClient
            ) throws ParserConfigurationException {
        log.info("Creating");
        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.documentRepository = documentRepository;
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.xPath = XPathFactory.newInstance().newXPath();
        this.objectMapper = new ObjectMapper();
            }

    // methods start here
    public List<String> getRemoteRecordList() throws IOException {
        String SITESSitemapURL = "https://meta.fieldsites.se/sitemap.xml";
        String SITESRecordExpression = "/urlset/url/loc";
        List<String> results = new ArrayList<String>();

        log.info("GET SITES sitemap at {}", SITESSitemapURL);

        try {
            XPathExpression SITESRecordXPath = xPath.compile(SITESRecordExpression);
            Document xmlSitemap = documentBuilder.parse(SITESSitemapURL);
            NodeList recordList = (NodeList) SITESRecordXPath.evaluate(xmlSitemap, XPathConstants.NODESET);
            int numRecords = recordList.getLength();
            for (int i=0; i<numRecords; i++){
                results.add(recordList.item(i).getTextContent());
            }
        } catch (SAXException | IOException ex) {
            // generic exceptions
            throw new IOException();
        } catch (XPathExpressionException ex) {
            log.error("This should never happen; the XPathExpression is fixed and valid.");
        }
        return results;
    }

    public Map<String, String> getLocalRecordMapping() throws IOException {
        // form and make SOLR query
        SolrDocumentList resultList = null;
        Map<String, String> resultMapping = new HashMap<String, String>(5000);

        log.info("GET locally imported records");

        try {
            SolrQuery query = new SolrQuery();
            query.setParam(CommonParams.Q, "importID:https\\://hdl.handle.net/11676.1/*");
            query.setParam(CommonParams.FL, "importID,identifier");
            // Ugh, there doesn't seem to be a way to return all results. To avoid
            // dealing with pagination just abort if 10000000 results are returned,
            // since we won't have checked all the records.
            //
            // At time of writing there are 881 records in the sitemap, so this
            // should basically never happen before the heat death of the universe.
            query.setRows(10000000);
            resultList = solrClient.query("documents", query, POST).getResults();
        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException ex) {
            // generic exceptions
            throw new IOException();
        }

        // raise warning and abort, as promised above
        if (resultList.getNumFound() >= 10000000L){
            log.error("10000000 results were returned: update the code in SITESImportService.java; aborting import");
            throw new IOException();
        }

        // populate mapping
        for (SolrDocument document : resultList){
            resultMapping.put(
                    (String) document.getFieldValue("importID"),
                    (String) document.getFieldValue("identifier")
                    );
        }

        return resultMapping;
    }

    public JsonNode getFullRemoteRecord(String remoteRecordID) throws IOException {
        log.info("GET record {}", remoteRecordID);

        // lazy exception handling for now
        try {
            // get record HTML as String
            // in the SITES sitemap, remoteRecordID is the URL to the record
            HttpHeaders headers = new HttpHeaders();
            ResponseEntity<String> response = restTemplate.exchange(
                    remoteRecordID,
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
        } catch (RestClientResponseException | IOException ex) {
            // generic exceptions
            throw new IOException();
        }
    }

    public String createRecord(String remoteRecordID, JsonNode parsedRecord, CatalogueUser user) throws DocumentRepositoryException {
        // prep
        ElterDocument newRecord = new ElterDocument();

        // set all the fields
        newRecord.setTitle(parsedRecord.get("name").asText());
        newRecord.setDescription(parsedRecord.get("description").asText());
        newRecord.setType("signpost");
        // import metadata
        newRecord.setImportID(parsedRecord.get("identifier").asText());
        newRecord.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));

        // save document
        MetadataDocument savedDocument = documentRepository.saveNew(
                user,
                newRecord,
                "elter",
                "Create new record " + remoteRecordID
                );

        // success
        log.info("Successfully imported record {}", remoteRecordID);
        return savedDocument.getId();
    }

    public void updateRecord(String localRecordID, String remoteRecordID, JsonNode parsedRecord, CatalogueUser user) throws DocumentRepositoryException {
        // prep
        ElterDocument newDocument = new ElterDocument();

        // set all the fields
        newDocument.setTitle(parsedRecord.get("name").asText());
        newDocument.setDescription(parsedRecord.get("description").asText());
        newDocument.setType("signpost");
        // import metadata
        newDocument.setImportID(parsedRecord.get("identifier").asText());
        newDocument.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));

        // save back
        newDocument.setMetadata(documentRepository.read(localRecordID).getMetadata());
        documentRepository.save(
                user,
                newDocument,
                localRecordID,
                "Updated record " + remoteRecordID
                );
        // success
        log.info("Successfully updated record {}", remoteRecordID);
    }

    //public void processRecord(String remoteRecordID){}

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport(){
        // prep
        log.info("Running SITES metadata import...");
        CatalogueUser importUser = new CatalogueUser().setUsername("SITES metadata import").setEmail("info@fieldsites.se");
        List<String> remoteRecordList = null;
        Map<String, String> localRecordList = null;

        // get remote records
        try {
            remoteRecordList = getRemoteRecordList();
        } catch (IOException ex) {
            log.error("Error retrieving remote records; aborting import");
            return;
        }

        // get local records
        try {
            localRecordList = getLocalRecordMapping();
        } catch (IOException ex) {
            log.error("Error retrieving locally imported records; aborting import");
            return;
        }

        // ready to import
        // do only 3 records while testing
        for (int i=0; i<3; i++){
            // get record
            String recordURL = remoteRecordList.get(i);
            JsonNode parsedRecord = null;
            try {
                parsedRecord = getFullRemoteRecord(recordURL);
            } catch (IOException ex) {
                log.error("Error retrieving record {}; skipping", recordURL);
                continue;
            }

            if (parsedRecord.get("@type").asText().equals("Dataset")){
                try {
                    String remoteRecordID = parsedRecord.get("identifier").asText();
                    if (localRecordList.containsKey(remoteRecordID)) {
                        updateRecord(localRecordList.get(remoteRecordID), remoteRecordID,  parsedRecord, importUser);
                    }
                    else {
                        String newID = createRecord(remoteRecordID, parsedRecord, importUser);
                        log.info("New document ID is {}", newID);
                    }
                } catch (DocumentRepositoryException ex) {
                    log.error("Error saving record {}; aborting import", recordURL);
                    return;
                }
            }
            else {
                log.info("Skipping record {} as it is not of type \"Dataset\"", recordURL);
            }
        }
    }
}
