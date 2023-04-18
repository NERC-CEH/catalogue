package uk.ac.ceh.gateway.catalogue.imports;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.time.LocalDate;
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
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;
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
        log.info("GET SITES sitemap at {}", sitemapUrl);

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
        log.info("GET locally imported records");

        // prep
        Map<String, String> resultMapping = new HashMap<>(5000);

        // form and make SOLR query
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
        log.info("GET record {}", remoteRecordId);

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
        // prep
        ElterDocument newRecord = new ElterDocument();

        // set fields from remote document
        newRecord.setTitle(parsedRecord.get("name").asText());
        newRecord.setDescription(parsedRecord.get("description").asText());
        // online resources
        ArrayList<OnlineResource> linkList = new ArrayList<>();
        linkList.add(
                OnlineResource.builder()
                .url(parsedRecord.get("url").asText())
                .name("View record")
                .description("View record at this link")
                .function("download")
                .build()
                );
        newRecord.setOnlineResources(linkList);
        // temporal extents
        ArrayList<TimePeriod> extentList = new ArrayList<>();
        String times = parsedRecord.get("temporalCoverage").asText();
        int split = times.indexOf("/");
        String beginTime = times.substring(0,10);
        String endTime = times.substring(split+1,split+11);
        extentList.add(
                TimePeriod.builder()
                .begin(beginTime)
                .end(endTime)
                .build()
                );
        // date published
        LocalDate published = LocalDate.parse(parsedRecord.get("datePublished").asText().substring(0,10));
        newRecord.setDatasetReferenceDate(
                DatasetReferenceDate.builder()
                .publicationDate(published)
                .build()
                );
        // contacts
        ArrayList<ResponsibleParty> contactList = new ArrayList<>();
        ResponsibleParty publisher = ResponsibleParty.builder()
                .organisationName("SITES data portal")
                .role("publisher")
                .email("info@fieldsites.se")
                .build();
        ResponsibleParty provider = ResponsibleParty.builder()
                .organisationName(parsedRecord.get("provider").get("name").asText())
                .role("resourceProvider")
                .email("info@fieldsites.se")
                .build();
        contactList.add(publisher);
        contactList.add(provider);
        newRecord.setResponsibleParties(contactList);

        // fixed stuff
        newRecord.setType("signpost");
        newRecord.setDataLevel("Level 0");
        newRecord.setAccessLimitation(
                AccessLimitation.builder()
                .value("no limitations to public access")
                .code("Available")
                .uri("http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations")
                .build()
                );
        // import metadata
        newRecord.setImportId(parsedRecord.get("identifier").asText());
        newRecord.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));

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
        log.info("Successfully imported record {}", remoteRecordId);
        return savedDocument.getId();
    }

    @SneakyThrows
    private void updateRecord(String localRecordId, String remoteRecordId, JsonNode parsedRecord, CatalogueUser user) {
        // prep
        ElterDocument updatedRecord = new ElterDocument();

        // set fields from remote document
        updatedRecord.setTitle(parsedRecord.get("name").asText());
        updatedRecord.setDescription(parsedRecord.get("description").asText());
        // online resources
        ArrayList<OnlineResource> linkList = new ArrayList<>();
        linkList.add(
                OnlineResource.builder()
                .url(parsedRecord.get("url").asText())
                .name("View record")
                .description("View record at this link")
                .function("download")
                .build()
                );
        updatedRecord.setOnlineResources(linkList);
        // temporal extents
        ArrayList<TimePeriod> extentList = new ArrayList<>();
        String times = parsedRecord.get("temporalCoverage").asText();
        int split = times.indexOf("/");
        String beginTime = times.substring(0,10);
        String endTime = times.substring(split+1,split+11);
        extentList.add(
                TimePeriod.builder()
                .begin(beginTime)
                .end(endTime)
                .build()
                );
        // date published
        LocalDate published = LocalDate.parse(parsedRecord.get("datePublished").asText().substring(0,10));
        updatedRecord.setDatasetReferenceDate(
                DatasetReferenceDate.builder()
                .publicationDate(published)
                .build()
                );
        // contacts
        ArrayList<ResponsibleParty> contactList = new ArrayList<>();
        ResponsibleParty publisher = ResponsibleParty.builder()
                .organisationName("SITES data portal")
                .role("publisher")
                .email("info@fieldsites.se")
                .build();
        ResponsibleParty provider = ResponsibleParty.builder()
                .organisationName(parsedRecord.get("provider").get("name").asText())
                .role("resourceProvider")
                .email("info@fieldsites.se")
                .build();
        contactList.add(publisher);
        contactList.add(provider);
        updatedRecord.setResponsibleParties(contactList);

        // fixed stuff
        updatedRecord.setType("signpost");
        updatedRecord.setDataLevel("Level 0");
        updatedRecord.setAccessLimitation(
                AccessLimitation.builder()
                .value("no limitations to public access")
                .code("Available")
                .uri("http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations")
                .build()
                );
        // import metadata
        updatedRecord.setImportId(parsedRecord.get("identifier").asText());
        updatedRecord.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));

        // save back
        updatedRecord.setMetadata(documentRepository.read(localRecordId).getMetadata());
        documentRepository.save(
                user,
                updatedRecord,
                localRecordId,
                "Updated record " + remoteRecordId
                );
        // success
        log.info("Successfully updated record {}", remoteRecordId);
    }

    //public void processRecord(String remoteRecordId){}

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport(){
        // prep
        log.info("Running SITES metadata import...");
        CatalogueUser importUser = new CatalogueUser().setUsername("SITES metadata import").setEmail("info@fieldsites.se");
        Map<String, String> localRecordList = null;

        // get remote records
        List<String> remoteRecordList = getRemoteRecordList();

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
                }
                else {
                    String newId = createRecord(remoteRecordId, parsedRecord, importUser);
                    log.info("New document ID is {}", newId);
                }
            }
            else {
                log.info("Skipping record {} as it is not of type \"Dataset\"", recordUrl);
            }
        }
    }
}
