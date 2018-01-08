import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BrowserWebDriverContainer;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class IntegrationTest {
    private static int WEB_PORT = 8080;
    private static String DOCKER_HOST = "172.21.0.1";

    @Rule
    public BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
        .withDesiredCapabilities(DesiredCapabilities.chrome());

    private final RestTemplate template = new RestTemplate();

    @Test
    @SneakyThrows
    public void getCapabilities() {
        ResponseEntity<String> response = template.getForEntity(
            "http://{host}:{port}/maps/{id}?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1",
            String.class,
            DOCKER_HOST,
            WEB_PORT,
            "mapserver-shapefile"
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be correct content type", response.getHeaders().getContentType().isCompatibleWith(MediaType.parseMediaType("application/vnd.ogc.wms_xml")), is(true));
    }

    @Test
    @SneakyThrows
    public void getTmsImage() {
        ResponseEntity<String> response = template.getForEntity(
            "http://{host}:{port}/documents/{id}/onlineResources/0/tms/1.0.0/{layer}/3/3/5.png",
            String.class,
            DOCKER_HOST,
            WEB_PORT,
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
            "http://{host}:{port}/maps/{id}?SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.1&LAYERS={layers}&STYLES=&FORMAT=image/png&HEIGHT=256&WIDTH=256&SRS={srs}&BBOX=0,0,700000,1300000",
            String.class,
            DOCKER_HOST,
            WEB_PORT,
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
            "http://{host}:{port}/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class,
            DOCKER_HOST,
            WEB_PORT
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
            "http://{host}:{port}/eidc/documents?term=land",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class,
            DOCKER_HOST,
            WEB_PORT
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
        String expectedTitle = "Woodlands survey flora data 1971-2001";
        RemoteWebDriver driver = chrome.getWebDriver();

        String docUrl = format(
            "http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae",
            DOCKER_HOST,
            WEB_PORT
        );

        //when
        driver.get(docUrl);

        //then
        String actualText = driver.findElementById("document-title").getText();
        assertThat("Title should be: Woodlands survey flora data 1971-2001", actualText, containsString(expectedTitle));

    }

    @Test
    @SneakyThrows
    public void getPublicationPage() {
        //given
        RemoteWebDriver driver = chrome.getWebDriver();

        String docUrl = format(
            "http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication",
            DOCKER_HOST,
            WEB_PORT
        );

        //when
        driver.get(docUrl);

        //then
        String actualTitle = driver.getTitle();
        assertThat("Page title should be correct", actualTitle, equalTo("Publication - Environmental Information Data Centre"));

    }

    @Test
    @SneakyThrows
    public void getPermissionPage() {
        //given
        RemoteWebDriver driver = chrome.getWebDriver();

        String docUrl = format(
            "http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/permission",
            DOCKER_HOST,
            WEB_PORT
        );

        //when
        driver.get(docUrl);

        //then
        String actualTitle = driver.getTitle();
        assertThat("Page title should be correct", actualTitle, equalTo("Permissions - Environmental Information Data Centre"));

    }


    @Test
    @SneakyThrows
    public void searchResults() {
        RemoteWebDriver driver = chrome.getWebDriver();
        String eidcUrl = format(
            "http://%s:%s/eidc/documents",
            DOCKER_HOST,
            WEB_PORT
        );

        driver.get(eidcUrl);
        int numRecords = parseInt(driver.findElementById("num-records").getText(), 10);

        assertThat("Should have found documents", numRecords, greaterThan(0));
    }
}
