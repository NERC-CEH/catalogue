package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.MetricsService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("DownloadController")
@WebMvcTest(
    controllers=DownloadController.class,
    properties="metrics.users.excluded=dummy,another,i_am_excluded"
)

public class DownloadControllerTest {

    @MockBean
    private MetricsService metricsService;

    @Autowired private MockMvc mvc;

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
}

