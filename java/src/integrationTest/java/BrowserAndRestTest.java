import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.util.Arrays;

import static java.lang.String.format;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
public class BrowserAndRestTest {
    private static String WEB_SERVICE = "web_1";
    private static int WEB_PORT = 8080;

    @ClassRule
    public static DockerComposeContainer environment = new DockerComposeContainer(new File("../docker-compose.yml"))
                                                            .withExposedService(WEB_SERVICE, WEB_PORT);

    private final RestTemplate template = new RestTemplate();

    @Test
    @SneakyThrows
    public void getCurrentPublicationStateHtml() {
        String url = format(
            "http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication",
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.TEXT_HTML, MediaType.ALL));
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be html", response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML), is(true));
    }

    @Test
    @SneakyThrows
    public void getCurrentPublicationStateJson() {
        String url = format(
            "http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication",
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be json", response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON), is(true));
    }

    @Test
    @SneakyThrows
    public void individualDocument() {
        String docUrl = format(
            "http://%s:%s/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae",
            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
            environment.getServicePort(WEB_SERVICE, WEB_PORT)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.TEXT_HTML, MediaType.ALL));
        ResponseEntity<String> response = template.exchange(docUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat("Should be html", response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML), is(true));

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

        assertThat("Term", term, equalTo("land"));
    }

//    @Test
//    @SneakyThrows
//    public void simpleTest() {
//        RemoteWebDriver driver = chrome.getWebDriver();
//
//        String eidcUrl = format(
//            "http://%s:%s/eidc/documents",
//            environment.getServiceHost(WEB_SERVICE, WEB_PORT),
//            environment.getServicePort(WEB_SERVICE, WEB_PORT)
//        );
//
//        log.info("web url: {}", eidcUrl);
//
//        ResponseEntity<String> response = template.getForEntity(eidcUrl, String.class);
//
//        assertThat("Response should be OK", response.getStatusCodeValue(), equalTo(HttpStatus.OK.value()));
//
//        driver.get(eidcUrl);
//        log.info("body: {}", driver.getPageSource());
//        WebElement searchInput = driver.findElementByName("term");
//
//        searchInput.sendKeys("land cover map");
//        searchInput.submit();
//
//        WebElement otherPage = driver.findElementByPartialLinkText("Land Cover Map");
//        otherPage.click();
//
//        String expectedText = driver.findElementById("document-description").getText();
//
//        assertThat("Should have found LCM2015", expectedText, containsString("LCM"));
//    }
}
