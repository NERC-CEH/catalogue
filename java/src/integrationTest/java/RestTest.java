import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Slf4j
public class RestTest {
    private final RestTemplate template;
    private final String webHost;
    private final String webPort;

    @SneakyThrows
    public RestTest() {
        template = new RestTemplate();
        webHost = System.getProperty("web.host");
        webPort = System.getProperty("web.tcp.8080");
    }

    @Test
    @SneakyThrows
    public void getRobots() {
        ResponseEntity<String> response = template.getForEntity(
            "http://{host}:{port}/robots.txt",
            String.class,
            webHost,
            webPort
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
    }

    @Test
    @SneakyThrows
    public void getEidcSitemap() {
        ResponseEntity<String> response = template.getForEntity(
            "http://{host}:{port}/eidc/sitemap.txt",
            String.class,
            webHost,
            webPort
        );
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
    }

    @Test(expected = HttpClientErrorException.class)
    @SneakyThrows
    public void getUnknownSitemap() {
        ResponseEntity<String> response = template.getForEntity(
            "http://{host}:{port}/unknown/sitemap.txt",
            String.class,
            webHost,
            webPort
        );
        fail("Response should be Not Found");
    }

    @Test
    @SneakyThrows
    public void getCapabilities() {
        ResponseEntity<String> response = template.getForEntity(
            "http://{host}:{port}/maps/{id}?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1",
            String.class,
            webHost,
            webPort,
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
            webHost,
            webPort,
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
            webHost,
            webPort,
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
            webHost,
            webPort
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
            webHost,
            webPort
        );

        //Then
        assertThat("Response should be OK", response.getStatusCode().is2xxSuccessful(), is(true));
        String term = JsonPath.read(response.getBody(), "$.term");
        assertThat("Term", term, IsEqual.equalTo("land"));
    }
}
