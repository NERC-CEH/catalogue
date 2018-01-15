import com.jayway.jsonpath.JsonPath;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.DockerComposeFiles;
import com.palantir.docker.compose.connection.DockerPort;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.Integer.parseInt;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class IntegrationTest {
    private final RestTemplate template = new RestTemplate();

    @ClassRule
    public static DockerComposeRule docker  = DockerComposeRule.builder()
        .files(DockerComposeFiles.from("../docker-compose.yml", "../docker-compose-chrome.yml"))
        .build();

    private int webPort() {
        return docker.containers()
            .container("web")
            .port(8080)
            .getExternalPort();
    }

    @SneakyThrows
    private RemoteWebDriver webDriver() {
        DockerPort chromePort = docker.containers().container("chrome").port(4444);
        URL url = new URL("http", "0.0.0.0", chromePort.getExternalPort(), "/wd/hub");
        ChromeOptions options = new ChromeOptions();
        return new RemoteWebDriver(url, options);
    }

    @Test
    @SneakyThrows
    public void getCapabilities() {
        ResponseEntity<String> response = template.getForEntity(
            "http://0.0.0.0:{port}/maps/{id}?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1",
            String.class,
            webPort(),
            "mapserver-shapefile"
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be correct content type", response.getHeaders().getContentType().isCompatibleWith(MediaType.parseMediaType("application/vnd.ogc.wms_xml")), is(true));
    }

    @Test
    @SneakyThrows
    public void getTmsImage() {
        ResponseEntity<String> response = template.getForEntity(
            "http://0.0.0.0:{port}/documents/{id}/onlineResources/0/tms/1.0.0/{layer}/3/3/5.png",
            String.class,
            webPort(),
            "mapserver-shapefile",
            "ukdata"
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be correct content type", response.getHeaders().getContentType().isCompatibleWith(MediaType.IMAGE_PNG), is(true));
    }

    @Test
    @SneakyThrows
    public void getMapImage() {
        ResponseEntity<String> response = template.getForEntity(
            "http://0.0.0.0:{port}/maps/{id}?SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.1&LAYERS={layers}&STYLES=&FORMAT=image/png&HEIGHT=256&WIDTH=256&SRS={srs}&BBOX=0,0,700000,1300000",
            String.class,
            webPort(),
            "mapserver-raster",
            "Band1",
            "EPSG:27700"
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be image/png", response.getHeaders().getContentType().isCompatibleWith(MediaType.IMAGE_PNG), is(true));
    }

    @Test
    @SneakyThrows
    public void getCurrentPublicationState() {
        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(of(MediaType.APPLICATION_JSON));

        //When
        ResponseEntity<String> response = template.exchange(
            "http://0.0.0.0:{port}/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class,
            webPort()
        );

        //Then
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be json", response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON), is(true));
    }

    @Test
    @SneakyThrows
    public void searchForLand() {
        //Given
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        //When
        ResponseEntity<String> response = template.exchange(
            "http://0.0.0.0:{port}/eidc/documents?term=land",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class,
            webPort()
        );

        //Then
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        String term = JsonPath.read(response.getBody(), "$.term");
        assertThat("Term", term, IsEqual.equalTo("land"));
    }

    @Test
    @SneakyThrows
    public void getIndividualDocument() {
        //given
        RemoteWebDriver driver = webDriver();
        String expectedTitle = "Woodlands survey flora data 1971-2001";

        //when
        driver.get("http://web:8080/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae");

        //then
        String actualText = driver.findElementById("document-title").getText();
        assertThat("Title should be: Woodlands survey flora data 1971-2001", actualText, containsString(expectedTitle));

    }

    @Test
    @SneakyThrows
    public void getPublicationPage() {
        //given
        RemoteWebDriver driver = webDriver();
        String expectedTitle = "Publication - Environmental Information Data Centre";

        //when
        driver.get("http://web:8080/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication");

        //then
        String actualTitle = driver.getTitle();
        assertThat("Page title should be correct", actualTitle, equalTo(expectedTitle));

    }

    @Test
    @SneakyThrows
    public void getPermissionPage() {
        //given
        RemoteWebDriver driver = webDriver();
        String expectedTitle = "Permissions - Environmental Information Data Centre";

        //when
        driver.get("http://web:8080/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/permission");

        //then
        String actualTitle = driver.getTitle();
        assertThat("Page title should be correct", actualTitle, equalTo(expectedTitle));

    }


    @Test
    @SneakyThrows
    public void searchResults() {
        //given
        RemoteWebDriver driver = webDriver();
        int minRecords = 0;

        //when
        driver.get("http://web:8080/eidc/documents");

        //then
        int numRecords = parseInt(driver.findElementById("num-records").getText(), 10);
        assertThat("Should have found documents", numRecords, greaterThan(minRecords));
    }
}
