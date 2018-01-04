package integration;

import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class IntegrationTest {
    private static String WEB_SERVICE = "web_1";
    private static int WEB_PORT = 8080;
    private static String DOCKER_HOST = "172.17.0.1";
    private static String DOCKER_COMPOSE = "../docker-compose.yml";

    @ClassRule
    public static DockerComposeContainer environment = new DockerComposeContainer(new File(DOCKER_COMPOSE))
                                                            .withExposedService(WEB_SERVICE, WEB_PORT);

    @Rule
    public BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
        .withDesiredCapabilities(DesiredCapabilities.chrome());

    private final RestTemplate template = new RestTemplate();

    @Test
    @SneakyThrows
    public void getCapabilities() {
        String url = "http://{host}:{port}/maps/{id}?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1";

        ResponseEntity<String> response = template.getForEntity(
            url,
            String.class,
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT),
            "mapserver-shapefile"
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be correct content type", response.getHeaders().getContentType().isCompatibleWith(MediaType.parseMediaType("application/vnd.ogc.wms_xml")), is(true));
    }

    @Test
    @SneakyThrows
    public void getTmsImage() {
        String url = "http://{host}:{port}/documents/{id}/onlineResources/0/tms/1.0.0/{layer}/3/3/5.png";

        ResponseEntity<String> response = template.getForEntity(
            url,
            String.class,
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT),
            "mapserver-shapefile",
            "ukdata"
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be correct content type", response.getHeaders().getContentType().isCompatibleWith(MediaType.IMAGE_PNG), is(true));
    }

    @Test
    @SneakyThrows
    public void getMapImage() {
        String url = "http://{host}:{port}/maps/{id}?SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.1&LAYERS={layers}&STYLES=&FORMAT=image/png&HEIGHT=256&WIDTH=256&SRS={srs}&BBOX=0,0,700000,1300000";

        ResponseEntity<String> response = template.getForEntity(
            url,
            String.class,
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT),
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
        String url = format(
            "http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication",
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(of(MediaType.APPLICATION_JSON));
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be json", response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON), is(true));
    }

    @Test
    @SneakyThrows
    public void searchForLand() {
        String searchUrl = format(
            "http://%s:%s/eidc/documents?term=land",
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        ResponseEntity<String> response = template.exchange(searchUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

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
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
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
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
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
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
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
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
        );

        driver.get(eidcUrl);
        int numRecords = parseInt(driver.findElementById("num-records").getText(), 10);

        assertThat("Should have found documents", numRecords, greaterThan(0));
    }
}
