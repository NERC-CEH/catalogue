package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@Slf4j
@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("DownloadController")


public @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties="metrics.users.excluded=dummy,another,i_am_excluded")
class DownloadControllerTest extends AbstractMvcTest {

    @NotNull @Value("${download.url.regexOrder}") private String orderUrlRegex;
    @NotNull @Value("${download.url.regexPackage}") private String packageUrlRegex;
    @NotNull @Value("${download.url.regexDatastore}") private String datastoreUrlRegex;
    @NotNull @Value("${download.url.regexCeda}") private String CedaUrlRegex;

    @MockitoBean
    private MetricsService metricsService;

    @Test
    @SneakyThrows
    public void validUrlDataPackage() {
        //given
        String uuid = "f36ecc5f-d6cc-4d00-b89a-9fea8f396d33";
        String url = "https://data-package.ceh.ac.uk/data/" + uuid + ".zip";

        //when
        mvc.perform(
                get("/download/{uuid}", uuid)
                    .param("url", URLEncoder.encode(url, StandardCharsets.UTF_8))
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(url));

        //then
        verify(metricsService).recordDownload(uuid, "127.0.0.1");
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser(username="i_am_excluded")
    public void excludedUser() {
        //given
        String uuid = "f36ecc5f-d6cc-4d00-b89a-9fea8f396d33";
        String url = "https://data-package.ceh.ac.uk/data/" + uuid + ".zip";

        //when
        mvc.perform(
                get("/download/{uuid}", uuid)
                    .param("url", URLEncoder.encode(url, StandardCharsets.UTF_8))
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(url));

        //then
        verify(metricsService, never()).recordDownload(any(), any());
    }

    @Test
    public void allowValidUrls() {
        //given
        List<String> validUrls = List.of(
            "https://catalogue.ceh.ac.uk/datastore/eidchub/abcdef12-3456-1234-0987-6876abcd1234",
            "https://data-package.ceh.ac.uk/data/c63c543f-3e95-4c1c-8c69-12f942271813",
            "https://order-eidc.ceh.ac.uk/resources/KBAHWTRW/order",
            "https://data.ceda.ac.uk/eidc/f5ce92b0-03e8-4719-82ff-c62e6ebe927b"
        );
        List<String> users = List.of("foo", "bar");
        DownloadController controller = new DownloadController(metricsService, users, orderUrlRegex, packageUrlRegex, datastoreUrlRegex, CedaUrlRegex);

        //when
        List<String> actual = validUrls.stream().filter(controller::valid).toList();

        //then
        assertThat("All urls should be valid", actual.size(), equalTo(4));
    }

    @Test
    public void disallowInvalidUrls() {
        //given
        List<String> validUrls = List.of(
            "https://www.google.com",
            "https://subdomain.domain.ac.uk",
            "https://invalid.com"
        );
        List<String> users = List.of("foo", "bar");
        DownloadController controller = new DownloadController(metricsService, users, orderUrlRegex, packageUrlRegex, datastoreUrlRegex, CedaUrlRegex);

        //when
        List<String> actual = validUrls.stream().filter(controller::valid).toList();

        //then
        assertThat("No urls should be valid", actual.size(), equalTo(0));
    }

}

