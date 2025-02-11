package uk.ac.ceh.gateway.catalogue.organisations;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.TimeConstants;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Slf4j
@Profile("solr:ror-update")
@Component
public class OrganisationUpdater {

    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String dataDumpUrl;
    private static final String COLLECTION = "organisations";
    private final String localPath;
    private final String configFile;
    private final String downloadKey = "latest.download";

    public OrganisationUpdater(
        @Qualifier("normal") RestTemplate restTemplate,
        SolrClient solrClient,
        @Value("${ror.url.dataDump}") String dataDumpUrl,
        @Value("${ror.local}") String localPath
    ) {
        this.restTemplate = restTemplate;
        this.dataDumpUrl = dataDumpUrl;
        this.solrClient = solrClient;
        this.localPath = localPath;
        this.configFile = localPath + "/config.properties";

    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void updateROROrganisation() {
        try {
            log.info("Start getting the latest ror download url");
            String response = restTemplate.getForObject(dataDumpUrl, String.class);
            JsonNode jsonNode = (new ObjectMapper()).readTree(response);
            JsonPointer jsonPointer = JsonPointer.compile("/hits/hits/0/files/0/links/self");
            String downloadUrl = jsonNode.at(jsonPointer).asText();
            log.info("Finish getting the latest ror download url, {}", downloadUrl);

            setupDataPath();
            Properties properties = new Properties();
            FileInputStream configInputStream = new FileInputStream(configFile);
            properties.load(configInputStream);
            String latestDownload = properties.getProperty(downloadKey);
            File dataFile = new File(localPath + "/ror_v2.csv");
            if (!latestDownload.equals(downloadUrl) || !dataFile.exists()) {
                log.info("Start downloading the latest ror file");
                InputStream downloadStream = new URL(downloadUrl).openStream();
                ZipInputStream zipStream = new ZipInputStream(downloadStream);
                ZipEntry entry;
                while ((entry = zipStream.getNextEntry()) != null) {
                    if (entry.getName().contains("ror-data_schema_v2.csv")) {
                        FileOutputStream dataOutputStream = new FileOutputStream(dataFile);
                        dataOutputStream.write(zipStream.readAllBytes());

                        properties.setProperty(downloadKey, downloadUrl);
                        FileOutputStream configOutputStream = new FileOutputStream(configFile);
                        properties.store(configOutputStream, "");

                        log.info("Finish downloading the latest ror file, {}", dataFile);
                    }
                }
            }

            log.info("Start updating ror file {} to solr", dataFile);
            solrClient.deleteByQuery(COLLECTION, "*:*");
            FileInputStream dataInputStream = new FileInputStream(dataFile);
            CSVReader csvReader = new CSVReader(new InputStreamReader(dataInputStream));
            int idIndex, nameIndex, acronymsIndex, aliasesIndex;
            idIndex = nameIndex = acronymsIndex = aliasesIndex = -1;
            String[] header;
            if ((header = csvReader.readNext()) != null) {
                for (int i = 0; i < header.length; i++) {
                    String title = header[i];
                    switch (title) {
                        case "id" -> idIndex = i;
                        case "names.types.ror_display" -> nameIndex = i;
                        case "names.types.acronym" -> acronymsIndex = i;
                        case "names.types.alias" -> aliasesIndex = i;
                    }
                }
            }

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                String id = line[idIndex].trim();
                String name = line[nameIndex].trim();
                List<String> acronyms = csvStr2List(line[acronymsIndex]);
                List<String> aliases = csvStr2List(line[aliasesIndex]);
                solrClient.addBean(COLLECTION, new Organisation(id, name, acronyms, aliases));
            }

            solrClient.commit(COLLECTION);
            log.info("Finish updating ror data to solr, {} documents", csvReader.getLinesRead() - 1);
        } catch (Exception e) {
            log.error("Failed to retrieve ror organisations, {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve ror organisation", e);
        }
    }

    private List<String> csvStr2List(String str) {
        return Arrays.stream(str.replaceFirst("^.+:", "").split(","))
            .map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private void setupDataPath() throws Exception {
        File data = new File(localPath);
        if (!data.exists()) {
            data.mkdirs();
        }
        File config = new File(configFile);
        if (!config.exists()) {
            Properties properties = new Properties();
            FileOutputStream os = new FileOutputStream(config);
            properties.setProperty(downloadKey, "");
            properties.store(os, "");
        }
    }
}
