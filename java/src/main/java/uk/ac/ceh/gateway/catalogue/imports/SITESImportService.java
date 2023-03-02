package uk.ac.ceh.gateway.catalogue.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
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
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

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
    public List<String> getRemoteRecordList() {
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
            log.error("Error retrieving sitemap - aborting...");
            // need to actually abort somehow
        } catch (XPathExpressionException ex) {
            log.error("This should never happen: the XPathExpression is fixed and valid.");
        }
        return results;
    }

    // can we use a generic or something here? along the lines of
    // public AbstractMetadataDocument getFullRemoteRecord(String recordID)
    public JsonNode getFullRemoteRecord(String recordID) throws RestClientResponseException, IOException, JsonProcessingException {
        log.info("GET record {}", recordID);
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
    }

    // dummy
    public String createRecord(String recordID){
        return "hello";
    }

    // dummy
    public String updateRecord(String recordID){
        return "hello";
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport() throws RestClientResponseException, IOException, JsonProcessingException {
        log.info("Running SITES metadata import...");
        // get sitemap
        List<String> remoteRecordList = getRemoteRecordList();
        // do only 3 records while testing
        for (int i=0; i<3; i++){
            // get record
            String recordID = remoteRecordList.get(i);
            JsonNode parsedRecord = getFullRemoteRecord(recordID);

            // log some info to console as proof-of-concept
            log.info(parsedRecord.get("@type").asText());
            log.info(parsedRecord.get("identifier").asText());
            log.info(parsedRecord.get("description").asText());
        }
    }
}
