package uk.ac.ceh.gateway.catalogue.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final XPath xPath;

    // constructor
    public SITESImportService(
            DocumentRepository documentRepository,
            @Qualifier("normal") RestTemplate restTemplate
            ) throws ParserConfigurationException {
        log.info("Creating");
        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.documentRepository = documentRepository;
        this.restTemplate = restTemplate;
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

    public JsonNode getFullRemoteRecord(String recordID) throws IOException {
        log.info("GET record {}", recordID);

        // lazy exception handling for now
        try {
            // get record HTML as String
            // in the SITES sitemap, recordID is the URL to the record
            HttpHeaders headers = new HttpHeaders();
            ResponseEntity<String> response = restTemplate.exchange(
                    recordID,
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

    public String createRecord(String recordID, JsonNode parsedRecord, CatalogueUser importUser) throws DocumentRepositoryException {
        ElterDocument newRecord = new ElterDocument();

        newRecord.setTitle(parsedRecord.get("name").asText());
        newRecord.setDescription(parsedRecord.get("description").asText());
        newRecord.setType("signpost");
        // import metadata
        newRecord.setImportID(parsedRecord.get("identifier").asText());
        newRecord.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));
        // save document
        MetadataDocument savedDocument = documentRepository.saveNew(
                importUser,
                newRecord,
                "elter",
                "Create new record " + recordID
                );
        // success
        log.info("Successfully imported record {}", recordID);
        return savedDocument.getId();
    }

    //public void updateRecord(String recordID, JsonNode parsedRecord, CatalogueUser importUser){}

    //public void processRecord(String recordID){}

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport(){
        // prep
        log.info("Running SITES metadata import...");
        CatalogueUser importUser = new CatalogueUser().setUsername("SITES metadata import").setEmail("info@fieldsites.se");
        List<String> remoteRecordList = null;

        // get sitemap
        try {
            remoteRecordList = getRemoteRecordList();
        } catch (IOException ex) {
            log.error("Error retrieving sitemap; aborting import");
            return;
        }

        // do only 3 records while testing
        for (int i=0; i<3; i++){
            // get record
            String recordID = remoteRecordList.get(i);
            JsonNode parsedRecord = null;
            try {
                parsedRecord = getFullRemoteRecord(recordID);
            } catch (IOException ex) {
                log.error("Error retrieving record {}; skipping", recordID);
                continue;
            }

            if (parsedRecord.get("@type").asText().equals("Dataset")){
                try {
                    String newID = createRecord(recordID, parsedRecord, importUser);
                    log.info("New document ID is {}", newID);
                } catch (DocumentRepositoryException ex) {
                    log.error("Error saving record {}; aborting import", recordID);
                    return;
                }
            }
            else {
                log.info("Skipping record {} as it is not of type \"Dataset\"",recordID);
            }
        }
    }
}
