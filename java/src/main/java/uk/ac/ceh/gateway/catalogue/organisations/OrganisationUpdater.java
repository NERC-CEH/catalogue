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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

    /**
     * The ROR columns kept, in the order they are written to the local CSV, keyed by the name the code
     * knows each by. The local CSV keeps ROR's own column names, so one header lookup reads both files.
     */
    private static final LinkedHashMap<String, String> COLUMNS = new LinkedHashMap<>();
    static {
        COLUMNS.put("url", "id");
        COLUMNS.put("name", "names.types.ror_display");
        COLUMNS.put("acronym", "names.types.acronym");
        COLUMNS.put("alias", "names.types.alias");
        COLUMNS.put("city", "locations.geonames_details.name");
        COLUMNS.put("region", "locations.geonames_details.country_subdivision_name");
        COLUMNS.put("country", "locations.geonames_details.country_name");
        COLUMNS.put("countryCode", "locations.geonames_details.country_code");
        COLUMNS.put("types", "types");
        COLUMNS.put("website", "links.type.website");
    }

    /** A ROR name's language, which prefixes each language's group of names: "en: York College" */
    private static final Pattern LANGUAGE_PREFIX = Pattern.compile("^\\s*(no_lang_code|[a-z]{2,3}):\\s*");

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
                // A CSV written before a column was added to COLUMNS is fetched again, as if the release were new
                if (!latestDownload.equals(downloadUrl) || !data.exists() || !hasAllColumns(data)) {
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

    /**
     * ROR groups names by language, "; " between groups and ", " between a group's names:
     * "cy: Canolfan Ecoleg a Hydroleg y DU; fr: Centre britannique pour l'Écologie et l'Hydrologie".
     * Drops each group's language, keeping every group.
     */
    private static String removeLanguages(String str) {
        return Arrays.stream(str.split(";"))
            .map(group -> LANGUAGE_PREFIX.matcher(group).replaceFirst("").trim())
            .filter(group -> !group.isBlank())
            .collect(Collectors.joining("; "));
    }

    /** A list from a column of names: split at both the groups' "; " and the names' ", " */
    private static List<String> csvStr2List(String str) {
        return Arrays.stream(str.split("[;,]"))
            .map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    /** The first of a column's "; "-separated values. Every active organisation has one location, but ROR allows several */
    private static String first(String str) {
        return Arrays.stream(str.split(";")).map(String::trim).filter(s -> !s.isBlank()).findFirst().orElse("");
    }

    private static String blankToNull(String str) {
        return str == null || str.isBlank() ? null : str.trim();
    }

    private HashMap<String, Integer> getHeaderIndex(String[] header) {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String title = header[i];
            for (Map.Entry<String, String> column : COLUMNS.entrySet()) {
                if (column.getValue().equals(title)) map.put(column.getKey(), i);
            }
            if ("status".equals(title)) map.put("status", i);
        }
        return map;
    }

    private static List<String> missingColumns(Map<String, Integer> map) {
        return COLUMNS.entrySet().stream()
            .filter(column -> !map.containsKey(column.getKey()))
            .map(Map.Entry::getValue)
            .toList();
    }

    /** Whether the local CSV has every column in COLUMNS, rather than being from before one was added */
    boolean hasAllColumns(File data) throws IOException {
        try (CSVReader csvReader = new CSVReader(new BufferedReader(new FileReader(data)))) {
            String[] header = csvReader.readNext();
            return header != null && missingColumns(getHeaderIndex(header)).isEmpty();
        } catch (com.opencsv.exceptions.CsvValidationException e) {
            return false;
        }
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
        try (InputStream downloadStream = downloadUrl.openStream();
             ZipInputStream zipStream = new ZipInputStream(downloadStream)) {
            return downloadFile(zipStream, data);
        }
    }

    private boolean downloadFile(ZipInputStream zipStream, File data) throws Exception {
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            if (entry.getName().contains(rorFileName)) {
                CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(zipStream)));
                String[] header;
                HashMap<String, Integer> map = null;
                if ((header = csvReader.readNext()) != null) {
                    map = getHeaderIndex(header);
                }
                if (map == null) continue;
                if (!missingColumns(map).isEmpty() || !map.containsKey("status")) {
                    log.error("ROR file {} lacks columns {}", entry.getName(), missingColumns(map));
                    continue;
                }
                try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(data)))) {
                    csvWriter.writeNext(COLUMNS.values().toArray(String[]::new));

                    String[] line;
                    while ((line = csvReader.readNext()) != null) {
                        if (!line[map.get("status")].equals("active")) continue;

                        csvWriter.writeNext(new String[] {
                            line[map.get("url")],
                            line[map.get("name")],
                            removeLanguages(line[map.get("acronym")]),
                            removeLanguages(line[map.get("alias")]),
                            first(line[map.get("city")]),
                            first(line[map.get("region")]),
                            first(line[map.get("country")]),
                            first(line[map.get("countryCode")]),
                            line[map.get("types")],
                            // Not split: a few websites have a ";jsessionid=" in them
                            line[map.get("website")].trim()
                        });
                    }
                }
                return true;
            }
        }
        return false;
    }

    public long updateToSolr(File data) throws Exception {
        try (CSVReader csvReader = new CSVReader(new BufferedReader(new FileReader(data)))) {
            String[] header = csvReader.readNext();
            HashMap<String, Integer> map = getHeaderIndex(header == null ? new String[0] : header);
            // Checked before anything is deleted: the collection soft-commits within seconds, so deleting
            // first and then failing on a file it can't read would leave the look-up empty until the
            // next ROR release
            List<String> missing = missingColumns(map);
            if (!missing.isEmpty()) {
                throw new IllegalStateException("ROR file " + data + " lacks columns " + missing + ", so it was not imported");
            }

            solrClient.deleteByQuery(COLLECTION, "*:*");

            String[] line;
            ArrayList<Organisation> beanList = new ArrayList<>();
            while ((line = csvReader.readNext()) != null) {
                beanList.add(Organisation.builder()
                    .id(line[map.get("url")].trim())
                    .name(line[map.get("name")].trim())
                    .acronyms(csvStr2List(line[map.get("acronym")]))
                    .aliases(csvStr2List(line[map.get("alias")]))
                    .city(blankToNull(line[map.get("city")]))
                    .region(blankToNull(line[map.get("region")]))
                    .country(blankToNull(line[map.get("country")]))
                    .countryCode(blankToNull(line[map.get("countryCode")]))
                    .types(Arrays.stream(line[map.get("types")].split(";")).map(String::trim).filter(s -> !s.isBlank()).toList())
                    .website(blankToNull(line[map.get("website")]))
                    .build());
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
}
