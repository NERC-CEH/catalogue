package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganisationUpdaterTest {

    @Mock private RestTemplate restTemplate;
    @Mock private SolrClient solrClient;

    OrganisationUpdater organisationUpdater;

    String TEST_LOCAL_PATH = "test_local_path";
    String dataDumpUrl = "http://dump-url";
    String downloadUrl = "http://download-url";
    String COLLECTION = "organisations";
    File data = new File(TEST_LOCAL_PATH + "/ror_v2.csv");

    @SneakyThrows
    private String getFileStr(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    /** The columns of a ROR v2 CSV, as in test-ror-data.zip */
    private static final String ROR_HEADER = "id,admin.created.date,admin.created.schema_version,admin.last_modified.date,admin.last_modified.schema_version,domains,established,external_ids.type.fundref.all,external_ids.type.fundref.preferred,external_ids.type.grid.all,external_ids.type.grid.preferred,external_ids.type.isni.all,external_ids.type.isni.preferred,external_ids.type.wikidata.all,external_ids.type.wikidata.preferred,links.type.website,links.type.wikipedia,locations.geonames_id,locations.geonames_details.continent_code,locations.geonames_details.continent_name,locations.geonames_details.country_code,locations.geonames_details.country_name,locations.geonames_details.country_subdivision_code,locations.geonames_details.country_subdivision_name,locations.geonames_details.lat,locations.geonames_details.lng,locations.geonames_details.name,names.types.acronym,names.types.alias,names.types.label,names.types.ror_display,ror_display_lang,relationships,status,types";

    @SneakyThrows
    private URL zipped(String csv) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("ror-data/ror-data.csv"));
            zip.write(csv.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        URL url = mock(URL.class);
        given(url.openStream()).willReturn(new ByteArrayInputStream(bytes.toByteArray()));
        return url;
    }

    @SneakyThrows
    private void writeData(String resource) {
        organisationUpdater.setupDataPath();
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream(resource));
             FileOutputStream os = new FileOutputStream(data)) {
            os.write(is.readAllBytes());
        }
    }

    @SneakyThrows
    private void recordLatestDownload(String url) {
        try (FileOutputStream os = new FileOutputStream(TEST_LOCAL_PATH + "/config.properties")) {
            java.util.Properties properties = new java.util.Properties();
            properties.setProperty("latest.download", url);
            properties.store(os, "");
        }
    }

    @BeforeEach
    public void setUp() {
        organisationUpdater = new OrganisationUpdater(
            restTemplate,
            solrClient,
            dataDumpUrl,
            TEST_LOCAL_PATH
        );
    }

    @AfterEach
    public void cleanUp() throws IOException {
        // Clean up the file after the test
        Path path = Paths.get(TEST_LOCAL_PATH);
        FileSystemUtils.deleteRecursively(path);
    }

    @Test
    @SneakyThrows
    public void testGetDownloadLink() {
        // Given
        String dataDumpUrlReturn = getFileStr("dataDumpUrlReturn.json");
        given(restTemplate.getForObject(dataDumpUrl, String.class)).willReturn(dataDumpUrlReturn);

        // When
        String downloadLink = organisationUpdater.getDownloadLink(dataDumpUrl);

        // Then
        assertEquals(downloadUrl, downloadLink);
    }

    @Test
    @SneakyThrows
    public void testDownloadFile() {
        // Given
        InputStream downloadStream = Objects.requireNonNull(getClass().getResourceAsStream("test-ror-data.zip"));
        URL downloadUrl = mock(URL.class);
        given(downloadUrl.openStream()).willReturn(downloadStream);

        // When
        organisationUpdater.setupDataPath();
        boolean status = organisationUpdater.downloadFile(downloadUrl, data);

        // Then
        assertTrue(status);
        String downloadFile = new String((new FileInputStream(data)).readAllBytes());
        assertEquals(getFileStr("ror_v2.csv"), downloadFile);
    }

    @Test
    @SneakyThrows
    public void testUpdateToSolr() {
        // Given
        organisationUpdater.setupDataPath();
        InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("ror_v2.csv"));
        FileOutputStream os = new FileOutputStream(data);
        os.write(is.readAllBytes());

        given(solrClient.deleteByQuery(eq(COLLECTION), anyString())).willReturn(null);
        given(solrClient.addBeans(eq(COLLECTION), anyList())).willReturn(null);
        given(solrClient.commit(COLLECTION)).willReturn(null);

        // When
        long count = organisationUpdater.updateToSolr(data);

        // Then
        assertEquals(2, count);
        verify(solrClient, times(1)).deleteByQuery(eq(COLLECTION), anyString());
        verify(solrClient, times(1)).addBeans(eq(COLLECTION), anyList());
        verify(solrClient, times(1)).commit(COLLECTION);
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void updateToSolrIndexesWhereEachOrganisationIs() {
        // Given
        writeData("ror_v2.csv");
        ArgumentCaptor<List<Organisation>> beans = ArgumentCaptor.forClass(List.class);

        // When
        organisationUpdater.updateToSolr(data);

        // Then
        verify(solrClient).addBeans(eq(COLLECTION), beans.capture());
        assertThat(beans.getValue(), contains(
            Organisation.builder()
                .id("https://ror.org/04ttjf776").name("RMIT University")
                .acronyms(List.of("RMIT")).aliases(List.of("Royal Melbourne Institute of Technology University"))
                .city("Melbourne").region("Victoria").country("Australia").countryCode("AU")
                .types(List.of("education", "funder")).website("https://www.rmit.edu.au/")
                .build(),
            Organisation.builder()
                .id("https://ror.org/01rxfrp27").name("La Trobe University")
                .acronyms(List.of()).aliases(List.of())
                .city("Melbourne").region("Victoria").country("Australia").countryCode("AU")
                .types(List.of("education", "funder")).website("http://www.latrobe.edu.au/")
                .build()
        ));
    }

    @Test
    @SneakyThrows
    public void updateToSolrLeavesTheCollectionAloneGivenAFileFromBeforeLocationsWereKept() {
        // Given a CSV in the four-column format written before dri-one #417
        writeData("ror_v2_four_columns.csv");

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> organisationUpdater.updateToSolr(data));

        // Then nothing was deleted, so the look-up keeps the organisations it had
        assertThat(thrown.getMessage(), containsString("locations.geonames_details.name"));
        verifyNoInteractions(solrClient);
    }

    @Test
    @SneakyThrows
    public void updateDownloadsTheSameReleaseAgainGivenAFileFromBeforeLocationsWereKept() {
        // Given the release has been downloaded, but the CSV on disk is in the old four-column format
        OrganisationUpdater spyUpdater = spy(organisationUpdater);
        doReturn(downloadUrl).when(spyUpdater).getDownloadLink(dataDumpUrl);
        doReturn(true).when(spyUpdater).downloadFile(downloadUrl, data);
        doReturn(2L).when(spyUpdater).updateToSolr(data);
        writeData("ror_v2_four_columns.csv");
        recordLatestDownload(downloadUrl);

        // When
        spyUpdater.updateOrganisation();

        // Then
        verify(spyUpdater).downloadFile(downloadUrl, data);
        verify(spyUpdater).updateToSolr(data);
    }

    @Test
    @SneakyThrows
    public void updateDoesNotDownloadTheSameReleaseAgainGivenACurrentFile() {
        // Given
        OrganisationUpdater spyUpdater = spy(organisationUpdater);
        doReturn(downloadUrl).when(spyUpdater).getDownloadLink(dataDumpUrl);
        doReturn(2L).when(spyUpdater).updateToSolr(data);
        writeData("ror_v2.csv");
        recordLatestDownload(downloadUrl);

        // When
        spyUpdater.updateOrganisation();

        // Then
        verify(spyUpdater, never()).downloadFile(anyString(), org.mockito.ArgumentMatchers.any(File.class));
        verify(spyUpdater).updateToSolr(data);
    }

    @Test
    @SneakyThrows
    public void downloadFileKeepsTheNamesInEveryLanguage() {
        // Given UKCEH, whose aliases are in two languages
        URL url = zipped(ROR_HEADER + "\n"
            + "https://ror.org/00pggkr55,,,,,,,,,,,,,,,https://www.ceh.ac.uk/,,,,,GB,United Kingdom,,England,,,Wallingford,"
            + "no_lang_code: UKCEH,\"cy: Canolfan Ecoleg a Hydroleg y DU; fr: Centre britannique pour l'Écologie et l'Hydrologie\","
            + "en: UK Centre for Ecology & Hydrology,UK Centre for Ecology & Hydrology,en,,active,funder; other\n");
        organisationUpdater.setupDataPath();

        // When
        assertTrue(organisationUpdater.downloadFile(url, data));

        // Then
        String written = new String(new FileInputStream(data).readAllBytes(), StandardCharsets.UTF_8);
        assertThat(written, containsString(
            "\"https://ror.org/00pggkr55\",\"UK Centre for Ecology & Hydrology\",\"UKCEH\","
            + "\"Canolfan Ecoleg a Hydroleg y DU; Centre britannique pour l'Écologie et l'Hydrologie\","
            + "\"Wallingford\",\"England\",\"United Kingdom\",\"GB\",\"funder; other\",\"https://www.ceh.ac.uk/\""));
    }

    @Test
    @SneakyThrows
    public void downloadFileWritesNothingGivenADumpWithoutLocations() {
        // Given
        URL url = zipped("id,names.types.ror_display,names.types.acronym,names.types.alias,status\n"
            + "https://ror.org/04ttjf776,RMIT University,,,active\n");
        organisationUpdater.setupDataPath();

        // When
        boolean downloaded = organisationUpdater.downloadFile(url, data);

        // Then
        assertFalse(downloaded);
        assertFalse(data.exists());
    }

    @Test
    @SneakyThrows
    public void testUpdateWithFileDownload() {
        // Given
        OrganisationUpdater spyUpdater = spy(organisationUpdater);
        doReturn(downloadUrl).when(spyUpdater).getDownloadLink(dataDumpUrl);
        doReturn(true).when(spyUpdater).downloadFile(downloadUrl, data);
        doReturn(2L).when(spyUpdater).updateToSolr(data);

        organisationUpdater.setupDataPath();
        InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("ror_v2.csv"));
        FileOutputStream os = new FileOutputStream(data);
        os.write(is.readAllBytes());

        // When
        spyUpdater.updateOrganisation();

        // Then
        verify(spyUpdater, times(1)).downloadFile(downloadUrl, data);
        verify(spyUpdater, times(1)).updateToSolr(data);
    }

    @Test
    @SneakyThrows
    public void testUpdateWithoutFileDownload() {
        // Given
        OrganisationUpdater spyUpdater = spy(organisationUpdater);
        doReturn(downloadUrl).when(spyUpdater).getDownloadLink(dataDumpUrl);
        doReturn(false).when(spyUpdater).downloadFile(downloadUrl, data);

        // When
        spyUpdater.updateOrganisation();

        // Then
        verify(spyUpdater, times(1)).downloadFile(downloadUrl, data);
        verify(spyUpdater, never()).updateToSolr(data);
    }
}
