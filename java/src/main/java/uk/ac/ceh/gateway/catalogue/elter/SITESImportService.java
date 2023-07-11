package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

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
public class SITESImportService implements CatalogueImportService {
    // constructor prep
    private final DocumentBuilder documentBuilder;
    private final DocumentRepository documentRepository;
    private final Map<String, List<DeimsSolrIndex>> stationToDeimsIndex;
    private final ObjectMapper objectMapper;
    private final PublicationService publicationService;
    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String sitemapUrl;
    private final XPathExpression xPath;

    private final Map<String, String> stationToDeimsId = Map.ofEntries(
            entry("https://meta.fieldsites.se/resources/stations/abisko", "64679f32-fb3e-4937-b1f7-dc25e327c7af"),
            entry("https://meta.fieldsites.se/resources/stations/Asa", "13b28889-ed32-495a-9bb6-a0886099e6d9"),
            entry("https://meta.fieldsites.se/resources/stations/bolmen", "e17d8c56-bf35-411d-a76b-061b6c7a9f0c"),
            entry("https://meta.fieldsites.se/resources/stations/Erken", "2c560a19-85bb-4e3b-b41f-9f1d06c6e0d6"),
            entry("https://meta.fieldsites.se/resources/stations/Grimso", "ba81bcc6-8916-47f3-a54a-5ac8ebe1c455"),
            entry("https://meta.fieldsites.se/resources/stations/Lonnstorp", "d733f936-b0b6-4bc1-9ab5-6cdb4081763a"),
            entry("https://meta.fieldsites.se/resources/stations/Robacksdalen", "7e2e2f68-989c-4e0a-8443-315ea48aac7f"),
            entry("https://meta.fieldsites.se/resources/stations/Skogaryd", "13f080f9-4831-4807-91da-bbfecb09a4f2"),
            entry("https://meta.fieldsites.se/resources/stations/Svartberget", "c0705d0f-92c1-4964-a345-38c0be3113e1"),
            entry("https://meta.fieldsites.se/resources/stations/Tarfala", "332a99af-8c02-4ce8-8f2b-70d17aaacf0a")
            );

    // constructor
    @SneakyThrows
    public SITESImportService(
            DocumentRepository documentRepository,
            PublicationService publicationService,
            @Qualifier("normal") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${sites.import.url}") String sitemapUrl
            ) {
        log.info("Creating");

        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.documentRepository = documentRepository;
        this.objectMapper = new ObjectMapper();
        this.publicationService = publicationService;
        this.restTemplate = restTemplate;
        this.sitemapUrl = sitemapUrl;
        this.solrClient = solrClient;
        this.stationToDeimsIndex = new HashMap<>();
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
    private List<DeimsSolrIndex> getDeimsSite(String id){
        SolrQuery query = new SolrQuery();
        query.setQuery(id);
        query.setParam(CommonParams.DF, "id");
        QueryResponse response = solrClient.query("deims", query, POST);
        return response.getBeans(DeimsSolrIndex.class);
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
    private ElterDocument createDocumentFromJson(JsonNode inputJson) {
        ElterDocument newDocument = new ElterDocument();
        // fields from JSON / import metadata
        newDocument.setTitle(inputJson.get("name").asText());
        newDocument.setDescription(inputJson.get("description").asText());
        newDocument.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));
        // some records have a list of identifiers, others just a string.
        // so we can't rely on the type of JsonNode of the identifier.
        // by using .path a missingNode will be returned and the conditionals can proceed
        // therefore if the identifier is not a string or array the identifier will be null
        JsonNode inputIdentifier = inputJson.path("identifier");
        if(inputIdentifier.isTextual()){
            newDocument.setImportId(inputIdentifier.asText());
        } else if(inputIdentifier.isArray()){
            for(int i=0; i < inputIdentifier.size(); i++){
                String testId = inputIdentifier.get(i).asText();
                if(testId.startsWith("https://hdl.handle.net/11676.1/")){
                    newDocument.setImportId(testId);
                    break;
                }
            }
        }
        // online resources
        ArrayList<OnlineResource> linkList = new ArrayList<>();
        linkList.add(
                OnlineResource.builder()
                .url(inputJson.get("url").asText())
                .name("View record")
                .description("View record at this link")
                .function("download")
                .build()
                );
        newDocument.setOnlineResources(linkList);
        // temporal extents
        ArrayList<TimePeriod> extentList = new ArrayList<>();
        String times = inputJson.get("temporalCoverage").asText();
        int split = times.indexOf("/");
        String beginTime = times.substring(0,10);
        String endTime = times.substring(split+1,split+11);
        extentList.add(
                TimePeriod.builder()
                .begin(beginTime)
                .end(endTime)
                .build()
                );
        newDocument.setTemporalExtents(extentList);
        // date published
        LocalDate published = LocalDate.parse(inputJson.get("datePublished").asText().substring(0,10));
        newDocument.setDatasetReferenceDate(
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
            .organisationName(inputJson.get("provider").get("name").asText())
            .role("resourceProvider")
            .email("info@fieldsites.se")
            .build();
        contactList.add(publisher);
        contactList.add(provider);
        newDocument.setResponsibleParties(contactList);
        // deims site
        newDocument.setDeimsSites(stationToDeimsIndex.get(inputJson.get("creator").get("@id").asText()));

        // fixed fields
        newDocument.setAccessLimitation(
                AccessLimitation.builder()
                .value("no limitations to public access")
                .code("Available")
                .uri("http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations")
                .build()
                );
        newDocument.setType("signpost");
        newDocument.setDataLevel("Level 0");

        return newDocument;
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
    private void updateRecord(String localRecordId, ElterDocument updatedRecord, CatalogueUser user) {
        String remoteRecordId = updatedRecord.getImportId();

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

        // get SITES DEIMS sites from SOLR
        for (String id : stationToDeimsId.keySet()){
            stationToDeimsIndex.put(
                    id,
                    getDeimsSite(stationToDeimsId.get(id))
                    );
        }

        // ready to import
        for (String recordUrl : remoteRecordList){
            JsonNode recordAsJson = getFullRemoteRecord(recordUrl);

            if (recordAsJson.get("@type").asText().equals("Dataset")){
                ElterDocument remoteRecord = createDocumentFromJson(recordAsJson);
                String remoteRecordId = remoteRecord.getImportId();
                if (remoteRecordId == null || !remoteRecordId.startsWith("https://hdl.handle.net/11676.1/")){
                    log.debug("Skipping record {} as no handle.net ID detected", recordUrl);
                    skippedRecords++;
                    continue;
                }
                if (localRecordList.containsKey(remoteRecordId)) {
                    updateRecord(localRecordList.get(remoteRecordId), remoteRecord, importUser);
                    updatedRecords++;
                }
                else {
                    String newId = createRecord(remoteRecordId, remoteRecord, importUser);
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
