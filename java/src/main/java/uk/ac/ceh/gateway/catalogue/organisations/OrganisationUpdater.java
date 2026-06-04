package uk.ac.ceh.gateway.catalogue.organisations;

import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.TimeConstants;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Slf4j
@Component
public class OrganisationUpdater {

    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String dataDumpUrl;
    private static final String COLLECTION = "organisations";
    private final String localPath;
    private final String configFile;
    private final String dataFile;
    private final String downloadKey = "latest.download";
    private String rorFileName = "";

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
        this.dataFile = localPath + "/ror_v2.csv";
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void updateOrganisation() {
        try {
            log.info("Start getting the latest ror download url");
            String downloadUrl = getDownloadLink(dataDumpUrl);
            log.info("Finish getting the latest ror download url, {}", downloadUrl);

            File data = new File(dataFile);
            if (!downloadUrl.isBlank()) {
                setupDataPath();
                Properties properties = new Properties();
                FileInputStream configInputStream = new FileInputStream(configFile);
                properties.load(configInputStream);
                String latestDownload = properties.getProperty(downloadKey);
                if (!latestDownload.equals(downloadUrl) || !data.exists()) {
                    log.info("Start downloading the latest ror file");
                    if (downloadFile(downloadUrl, data)) {
                        properties.setProperty(downloadKey, downloadUrl);
                        FileOutputStream configOutputStream = new FileOutputStream(configFile);
                        properties.store(configOutputStream, "");

                        log.info("Finish downloading the latest ror file, {}", data);
                    }
                }
            }
            data = new File(dataFile);
            if (data.exists()) {
                log.info("Start updating ror file {} to solr", data);
                long recordUpdated = updateToSolr(data);
                log.info("Finish updating ror data to solr, {} documents", recordUpdated);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve ror organisations, {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve ror organisation", e);
        }
    }

    private String removeUnrelatedStr(String str) {
        return str.replaceFirst("^.+:", "").trim();
    }

    private List<String> csvStr2List(String str) {
        return Arrays.stream(str.split(","))
            .map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private HashMap<String, Integer> getHeaderIndex(String[] header) {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String title = header[i];
            switch (title) {
                case "id" -> map.put("url", i);
                case "names.types.ror_display" -> map.put("name", i);
                case "names.types.acronym" -> map.put("acronym", i);
                case "names.types.alias" -> map.put("alias", i);
                case "status" -> map.put("status", i);
            }
        }
        return map;
    }

    public void setupDataPath() throws Exception {
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

    public String getDownloadLink(String dataDumpUrl) {
        String response = restTemplate.getForObject(dataDumpUrl, String.class);
        JsonNode jsonNode = (new ObjectMapper()).readTree(response);

        JsonPointer fileNamePointer = JsonPointer.compile("/hits/hits/0/files/0/key");
        rorFileName = jsonNode.at(fileNamePointer).asString().replace(".zip", ".csv");

        JsonPointer downloadLinkPointer = JsonPointer.compile("/hits/hits/0/files/0/links/self");
        return jsonNode.at(downloadLinkPointer).asString();
    }

    public boolean downloadFile(String downloadUrl, File data) throws Exception {
        URI uri = new URI(downloadUrl);
        return downloadFile(uri.toURL(), data);
    }

    public boolean downloadFile(URL downloadUrl, File data) throws Exception {
        InputStream downloadStream = downloadUrl.openStream();
        ZipInputStream zipStream = new ZipInputStream(downloadStream);
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            if (entry.getName().contains(rorFileName)) {
                CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(zipStream)));
                String[] header;
                HashMap<String, Integer> map = null;
                if ((header = csvReader.readNext()) != null) {
                    map = getHeaderIndex(header);
                }
                if (map != null && !map.isEmpty()) {
                    CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(data)));
                    csvWriter.writeNext(new String[] {header[map.get("url")], header[map.get("name")], header[map.get("acronym")], header[map.get("alias")]});

                    String[] line;
                    while ((line = csvReader.readNext()) != null) {
                        if (!line[map.get("status")].equals("active")) continue;

                        csvWriter.writeNext(new String[] {
                            line[map.get("url")],
                            line[map.get("name")],
                            removeUnrelatedStr(line[map.get("acronym")]),
                            removeUnrelatedStr(line[map.get("alias")])
                        });
                    }

                    csvWriter.close();
                    return true;
                }
            }
        }
        return false;
    }

    public long updateToSolr(File data) throws Exception {
        solrClient.deleteByQuery(COLLECTION, "*:*");

        CSVReader csvReader = new CSVReader(new BufferedReader(new FileReader(data)));
        HashMap<String, Integer> map = getHeaderIndex(csvReader.readNext());
        String[] line;
        ArrayList<Organisation> beanList = new ArrayList<>();
        while ((line = csvReader.readNext()) != null) {
            String id = line[map.get("url")].trim();
            String name = line[map.get("name")].trim();
            List<String> acronyms = csvStr2List(line[map.get("acronym")]);
            List<String> aliases = csvStr2List(line[map.get("alias")]);
            beanList.add(new Organisation(id, name, acronyms, aliases));
            if (beanList.size() >= 3000) {
                solrClient.addBeans(COLLECTION, beanList);
                beanList = new ArrayList<>();
            }
        }
        if (!beanList.isEmpty()) {
            solrClient.addBeans(COLLECTION, beanList);
        }
        solrClient.commit(COLLECTION);

        return csvReader.getLinesRead() - 1;
    }
}
