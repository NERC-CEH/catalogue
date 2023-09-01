package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrIndex;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.imports.CatalogueImportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@Profile("server:elter & imports")
@Slf4j
@Service
@ToString
public class B2shareImportService implements CatalogueImportService {
    // constructor prep
    private final DateTimeFormatter dateParser;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final Pattern deimsIdNormalise;
    private final Pattern doiNormalise;
    private final PublicationService publicationService;
    private final SolrClient solrClient;
    private final String b2shareRecordsFirstPageUrl;

    // fixed fields
    private static final AccessLimitation openAccess = AccessLimitation.builder()
        .value("no limitations to public access")
        .code("Available")
        .uri("http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations")
        .build();
    private static final AccessLimitation controlledAccess = AccessLimitation.builder()
        .value("To access this data, a licence needs to be negotiated with the provider and there may be a cost")
        .code("Controlled")
        .build();
    private static final List<String> blacklistedDois = List.of(
            "10.23728/b2share.09454896da99494f931be25e279658ef",
            "10.23728/b2share.16b4760bb98642fc97730e32bac39e63",
            "10.23728/b2share.192a897c521b4c6babfc1b9db65163b2",
            "10.23728/b2share.1bd8c763c4524168aecafad6d941c5c0",
            "10.23728/b2share.227bede4ca97433bb86405ad30c8b0f4",
            "10.23728/b2share.2854520d9e4947e68ddb6a0a270a4b5f",
            "10.23728/b2share.2f3df5f7baf04288853f24121d0810cd",
            "10.23728/b2share.34a1f1b96ab6404983a3da0f3e13713f",
            "10.23728/b2share.362d6bae28094f788463f6ada0a5203b",
            "10.23728/b2share.388b113860ae4b4da48214c18f30b91f",
            "10.23728/b2share.399c0653601a46488082e0f5edafc0df",
            "10.23728/b2share.426cc15f73dc47b98b6b2af9d740ea98",
            "10.23728/b2share.44219e37ca9045779ede02ac4feb418a",
            "10.23728/b2share.58dde320216e41a5a97b8f4b287efb4b",
            "10.23728/b2share.58eb7b9a8b3c466783762cb15dcd3898",
            "10.23728/b2share.5919b9ca10044e64aef836f448d69a3e",
            "10.23728/b2share.5b175810bd504f2f8607a9ed9f078809",
            "10.23728/b2share.5ed401dfe7274f268fcfed2aa595df70",
            "10.23728/b2share.6175e4fe23194eed8f63325cae7b1131",
            "10.23728/b2share.61c4fc815b044ed2a80321fef27c7d32",
            "10.23728/b2share.62e30863d25542f5a9e6e7ce00d08b9f",
            "10.23728/b2share.63ff6e748ff34edd8b314805c73b7ffe",
            "10.23728/b2share.6436f257b9e44c3c81e614e6d68c5083",
            "10.23728/b2share.6754e1d72a75478090a84216c0321d4d",
            "10.23728/b2share.68d0d103ee904b3886ac19d455f4089a",
            "10.23728/b2share.69b11c4e7a3b4762b80951728b030ba5",
            "10.23728/b2share.6de1e903ae0445a487cb219142bbcfb2",
            "10.23728/b2share.79905f4696814887a7d186643c30f962",
            "10.23728/b2share.81f34962de634f5e9effcacba3b40f97",
            "10.23728/b2share.8c4b87b485f5493bbfbcd1ce1ee6dd6e",
            "10.23728/b2share.8d628873b8c147f09a3b74090ff65b08",
            "10.23728/b2share.913069fbb66740a7a80d5de7ce32edb7",
            "10.23728/b2share.914c38d9675149c8bc18e0be48cf6ef3",
            "10.23728/b2share.9752dbeeab904279a422d7fae4b31ae4",
            "10.23728/b2share.9a664abd7ebb4db6b26d9d955147490e",
            "10.23728/b2share.9a8aa6c218e54a3d8c08e3912e6364de",
            "10.23728/b2share.a6ef9b030de240f198581261be9de6c1",
            "10.23728/b2share.ab59b8caa1aa484bbdcaefad9a9a7437",
            "10.23728/b2share.b31d67e6a3a6468db8a3f2939ecbbe4c",
            "10.23728/b2share.b36f78e35b4a42439f0e3730323cf8c3",
            "10.23728/b2share.b9a0acc25ca74ec9ace2bdc9a6799e97",
            "10.23728/b2share.c4124d84f54b4d09826236514102003a",
            "10.23728/b2share.c7ffe7052f2f4f73bc2f1770e6716d7d",
            "10.23728/b2share.cafe3eb1d4ef4e648bd4f8f058ab5dfb",
            "10.23728/b2share.d84a0855ef7241839da4b0f4644e8553",
            "10.23728/b2share.dde42ccc994b4702b396225d58b0049a",
            "10.23728/b2share.df83c69cc955416f9fb847ba86b3141b",
            "10.23728/b2share.ebd200fa5dac41dc93a3db74b19087be",
            "10.23728/b2share.ec4fa718a1a1482cae299268615668f0",
            "10.23728/b2share.f42f4fec3729435eb371bb67825715d0",
            "10.23728/b2share.f68dd6f4a9384b449a8d6ebb18ae02bb",
            "10.23728/b2share.f97c8289132b4ba19a3e21c5032c910b",
            "10.23728/b2share.fa82d241391c4b9e867731b3b9e82f7d",
            "10.23728/b2share.fadbfb30fdd543f4a3af31130d6dc685"
            );

    // constructor
    @SneakyThrows
    public B2shareImportService(
            DocumentRepository documentRepository,
            PublicationService publicationService,
            SolrClient solrClient,
            @Value("${b2share.api}") String b2shareRecordsFirstPageUrl
            ) {
        log.info("Creating");

        this.b2shareRecordsFirstPageUrl = b2shareRecordsFirstPageUrl;
        this.dateParser = new DateTimeFormatterBuilder()
            // order matters! put patterns containing others first
            .appendPattern("[y-M-d'T'H:m:s.nXXX]")
            .appendPattern("[y-M-d'T'H:m:s.nxxx]")
            .appendPattern("[y-M-d]")
            .appendPattern("[d.M.yyyy]")
            .appendPattern("[d/M/yyyy]")
            .toFormatter();
        this.documentRepository = documentRepository;
        this.objectMapper = new ObjectMapper();
        this.deimsIdNormalise = Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
        this.doiNormalise = Pattern.compile("10\\.\\S+/\\S+");
        this.publicationService = publicationService;
        this.solrClient = solrClient;
        }

    // methods start here
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
    private List<DeimsSolrIndex> getDeimsSite(String id){
        SolrQuery query = new SolrQuery();
        query.setQuery(id);
        query.setParam(CommonParams.DF, "id");
        QueryResponse response = solrClient.query("deims", query, POST);
        return response.getBeans(DeimsSolrIndex.class);
    }

    @SneakyThrows
    private ElterDocument createDocumentFromJson(JsonNode inputJson) {
        // create document from B2SHARE API JSON
        ElterDocument newDocument = new ElterDocument();
        JsonNode metadataJson = inputJson.get("metadata");

        // fields from JSON / import metadata
        // ensure title is set to something
        JsonNode jsonTitles = metadataJson.path("titles");
        int numTitles = jsonTitles.size();
        if (numTitles == 0){
            newDocument.setTitle("TITLE MISSING");
        }
        else {
            newDocument.setTitle(jsonTitles.get(0).get("title").asText());
            List<String> alternativeTitles = new ArrayList<>();
            for (int i = 1; i < numTitles; i++){
                alternativeTitles.add(jsonTitles.get(i).get("title").asText());
            }
            newDocument.setAlternateTitles(alternativeTitles);
        }
        // description
        StringBuilder descriptionBuilder = new StringBuilder();
        for (JsonNode node : metadataJson.path("descriptions")) {
            if (descriptionBuilder.length() > 0){
                descriptionBuilder.append("\n\n");
            }
            JsonNode descriptionTypeNode = node.get("description_type");
            if (descriptionTypeNode != null) {
                String descriptionType = descriptionTypeNode.asText();
                if (! descriptionType.equals("Other")) {
                    descriptionBuilder.append(descriptionType + ": ");
                }
            }
            descriptionBuilder.append(node.get("description").asText().strip());
        }
        newDocument.setDescription(descriptionBuilder.toString());
        // authors and contact_email (separate field)
        List<ResponsibleParty> contactList = new ArrayList<>();
        // creators
        JsonNode creators = metadataJson.path("creators");
        if (! creators.isMissingNode()) {
            for (JsonNode creatorNode : creators) {
                ResponsibleParty creator = ResponsibleParty.builder()
                        .individualName(creatorNode.get("creator_name").asText())
                        .role("author")
                        .organisationName("Unknown")
                        .build();
                contactList.add(creator);
            }
        }
        // email
        JsonNode contactEmail = metadataJson.path("contact_email");
        if (! contactEmail.isMissingNode()) {
            ResponsibleParty contact = ResponsibleParty.builder()
                .email(contactEmail.asText())
                .role("pointOfContact")
                .build();
            contactList.add(contact);
        }
        newDocument.setResponsibleParties(contactList);
        // onlineresources
        List<OnlineResource> resources = new ArrayList<>();
        resources.add(
                OnlineResource.builder()
                .url("https://b2share.eudat.eu/records/" + inputJson.get("id").asText())
                .name("View record")
                .description("View record at this link")
                .function("information")
                .build()
                );
        newDocument.setOnlineResources(resources);
        // reference dates
        LocalDate created = null;
        LocalDate published = null;
        String createdTimestamp = inputJson.get("created").asText();
        String publishedTimestamp = metadataJson.path("publication_date").asText();
        try {
            created = LocalDate.parse(createdTimestamp, dateParser);
        } catch (DateTimeParseException e) {
            log.debug("invalid created date {}", createdTimestamp);
        }
        try {
            if (!publishedTimestamp.equals("")) {
                published = LocalDate.parse(publishedTimestamp, dateParser);
            }
        } catch (DateTimeParseException e) {
            log.debug("invalid publication_date date {}", publishedTimestamp);
        }
        newDocument.setDatasetReferenceDate(
                DatasetReferenceDate.builder()
                .creationDate(created)
                .publicationDate(published)
                .build()
                );
        // supplemental / DOI link
        String recordDoi = metadataJson.get("DOI").asText();
        List<Supplemental> supplemental = new ArrayList<>();
        supplemental.add(
                Supplemental.builder()
                .name(recordDoi)
                .description("Resolve record DOI at doi.org")
                .url(recordDoi)
                .build()
                );
        newDocument.setSupplemental(supplemental);
        // access
        boolean isOpenAccess = metadataJson.get("open_access").booleanValue();
        if (isOpenAccess) {
            newDocument.setAccessLimitation(openAccess);
        } else {
            newDocument.setAccessLimitation(controlledAccess);
        }
        // deims site
        String metadataUrl = metadataJson
            .path("community_specific")
            .path("27193e5b-97e6-4f6f-8e87-3694589bcebe")
            .path("metadata_url")
            .asText()
            .toLowerCase();
        Matcher deimsCheck = deimsIdNormalise.matcher(metadataUrl);
        if (deimsCheck.find()) {
            String normalisedDeimsId = deimsCheck.group(0);
            newDocument.setDeimsSites(getDeimsSite(normalisedDeimsId));
        }
        // disciplines and keywords
        List<Keyword> keywords = new ArrayList<>();
        for (JsonNode node : metadataJson.path("keywords")) {
            Keyword keyword = Keyword.builder()
                .value(node.get("keyword").asText())
                .build();
            keywords.add(keyword);
        }
        for (JsonNode node : metadataJson.path("disciplines")) {
            Keyword keyword = Keyword.builder()
                .value(node.get("discipline_name").asText())
                .build();
            keywords.add(keyword);
        }
        newDocument.setKeywords(keywords);
        // import metadata
        newDocument.setImportLastModified(ZonedDateTime.now(ZoneId.of("UTC")));

        // fixed fields
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

    @SneakyThrows
    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void runImport(){
        // prep
        log.info("Running B2SHARE metadata import...");
        CatalogueUser importUser = new CatalogueUser().setUsername("B2SHARE metadata import").setEmail("info@eudat.eu");
        Map<String, String> localRecordList = null;
        int blacklistedRecords = 0;
        int newRecords = 0;
        int publishedRecords = 0;
        int skippedRecords = 0;
        int totalRecords = 0;
        int updatedRecords = 0;

        // get local records
        try {
            localRecordList = getLocalRecordMapping();
        } catch (IOException ex) {
            log.error("Error retrieving locally imported records; aborting import");
            return;
        }

        // ready to import
        JsonNode b2shareRecordsPage = null;
        String b2shareRecordsnextPageUrl = b2shareRecordsFirstPageUrl;
        while (! b2shareRecordsnextPageUrl.equals("")) {
            // get next page of records
            b2shareRecordsPage = objectMapper.readTree(new URL(b2shareRecordsnextPageUrl));

            for (JsonNode record : b2shareRecordsPage.path("hits").path("hits")){
                // process each record on page
                // normalise DOI to actual DOI, i.e. "10.xxx.../xxxx"
                String originalDoi = record.path("metadata").path("DOI").asText();
                Matcher doiCheck = doiNormalise.matcher(originalDoi);
                if (!doiCheck.find()) {
                    log.debug("No DOI detected in record {}", originalDoi);
                    skippedRecords++;
                    continue;
                }
                String normalisedDoi = doiCheck.group(0);
                if (blacklistedDois.contains(normalisedDoi)) {
                    log.debug("Skipping blacklisted doi {}", normalisedDoi);
                    skippedRecords++;
                    blacklistedRecords++;
                    continue;
                }

                // ready to import, as we have a record and a correctly-structured DOI
                // to use as the importId
                ElterDocument recordDocument = createDocumentFromJson(record);
                recordDocument.setImportId(normalisedDoi);

                if (localRecordList.containsKey(normalisedDoi)) {
                    updateRecord(localRecordList.get(normalisedDoi), normalisedDoi,  recordDocument, importUser);
                    updatedRecords++;
                }
                else {
                    String newId = createRecord(normalisedDoi, recordDocument, importUser);
                    log.debug("New document ID is {}", newId);
                    newRecords++;

                    // publish new record IF it has a DEIMS site linked to it
                    List<DeimsSolrIndex> deimsSites = recordDocument.getDeimsSites();
                    if (deimsSites != null && deimsSites.size() > 0) {
                        publicationService.transition(importUser, newId, "ykhm7b");
                        publicationService.transition(importUser, newId, "re4vkb");
                        log.debug("Successfully detected DEIMS ID and published record {}", recordDocument.getId());
                        publishedRecords++;
                    }
                }
            }
            b2shareRecordsnextPageUrl = b2shareRecordsPage.path("links").path("next").asText();
        }
        totalRecords = b2shareRecordsPage.get("hits").get("total").asInt();

        // finished, log summary
        log.info("Finished B2SHARE metadata import!");
        log.info("{} created ({} published) + {} updated + {} skipped = {} total ({} records in B2SHARE)",
                newRecords,
                publishedRecords,
                updatedRecords,
                skippedRecords,
                newRecords + updatedRecords + skippedRecords,
                totalRecords
                );
        log.info("{}/{} blacklisted DOIs were skipped", blacklistedRecords, blacklistedDois.size());
    }
}
