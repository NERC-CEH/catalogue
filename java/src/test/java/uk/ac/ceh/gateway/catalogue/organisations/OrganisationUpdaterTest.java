package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
