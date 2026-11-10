package uk.ac.ceh.gateway.catalogue.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import uk.ac.ceh.gateway.catalogue.indexing.jena.JenaIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexingService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;

/**
 * The kubelet's startup, liveness and readiness probes send no {@code Accept} header. The actuator then
 * answers in its own type, {@code application/vnd.spring-boot.actuator.v3+json}, and the narrowed JSON
 * converter ({@link WebConfig#RECORD_JSON_MEDIA_TYPES}) must not reach it there, or every probe is a 500 and
 * no pod becomes Ready (dri-one #527). {@code ManagementJsonConfig} undoes the narrowing in the management
 * context only, so the application port must keep it.
 * <p>
 * Real HTTP against the real management port: the actuator runs in its own child context there, which
 * MockMvc on the application context would not reach.
 */
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("Actuator health probes, end to end")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorHealthEndToEndTest {

    @LocalManagementPort
    private int managementPort;

    // Keep the context from indexing on start-up
    @MockitoBean @Qualifier("solr-index") SolrIndexingService indexService;
    @MockitoBean @Qualifier("jena-index") JenaIndexingService linkingService;
    @MockitoBean @Qualifier("mapserver-index") MapServerIndexingService mapserverService;

    // The application context's own adapter: the test instance is wired from the main context, not the child
    @Autowired @Qualifier("requestMappingHandlerAdapter") RequestMappingHandlerAdapter applicationAdapter;

    private final HttpClient client = HttpClient.newHttpClient();

    static Stream<Arguments> probes() {
        return Stream.of("/actuator/health/liveness", "/actuator/health/readiness").flatMap(path -> Stream.of(
            Arguments.of(path, null, "application/vnd.spring-boot.actuator.v3+json"),
            Arguments.of(path, "*/*", "application/vnd.spring-boot.actuator.v3+json"),
            Arguments.of(path, "application/vnd.spring-boot.actuator.v3+json", "application/vnd.spring-boot.actuator.v3+json"),
            Arguments.of(path, "application/vnd.spring-boot.actuator.v2+json", "application/vnd.spring-boot.actuator.v2+json"),
            Arguments.of(path, "application/json", "application/json")
        ));
    }

    @ParameterizedTest(name = "{0} with Accept {1}")
    @MethodSource("probes")
    @SneakyThrows
    void healthProbeAnswers(String path, String accept, String expectedType) {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + managementPort + path)).GET();
        if (accept != null) {
            request.header("Accept", accept);
        }

        var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode(), is(200));
        assertThat(response.headers().firstValue("Content-Type").orElse(""), containsString(expectedType));
        assertThat(response.body(), containsString("\"status\":\"UP\""));
    }

    @Test
    @DisplayName("the application port keeps the record narrowing")
    void applicationPortKeepsTheRecordNarrowing() {
        var jsonConverters = applicationAdapter.getMessageConverters().stream()
            .filter(converter -> converter.getClass() == JacksonJsonHttpMessageConverter.class)
            .toList();

        assertThat(jsonConverters, is(not(empty())));
        jsonConverters.forEach(converter ->
            assertThat(converter.getSupportedMediaTypes(), contains(WebConfig.RECORD_JSON_MEDIA_TYPES.toArray())));
    }
}
