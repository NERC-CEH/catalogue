package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.config.CatalogueServiceConfig;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("RootRedirectController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class,
    CatalogueServiceConfig.class
})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RootRedirectControllerTest extends AbstractMvcTest {

    /**
     * The port is given explicitly because {@code MockHttpServletRequestBuilder} otherwise leaves it at
     * 80, and an https request on port 80 is not a request anything makes. It used to pass regardless,
     * since the workaround this replaces discarded the port along with the scheme.
     */
    @Test
    @SneakyThrows
    void redirectToEidc() {
        //when
        mvc.perform(get("https://catalogue.ceh.ac.uk:443/").secure(true))
            .andExpect(redirectedUrl("https://catalogue.ceh.ac.uk/eidc/documents"));
    }

    @Test
    @DisplayName("follows the forwarded scheme when TLS was terminated upstream (dri-one #71, #260)")
    @SneakyThrows
    void redirectFollowsForwardedProto() {
        //when the ingress has terminated TLS and forwarded plain HTTP, saying so in the headers
        mvc.perform(get("http://catalogue.ceh.ac.uk/")
                .header("X-Forwarded-Proto", "https")
                .header("X-Forwarded-Port", "443"))
           .andExpect(redirectedUrl("https://catalogue.ceh.ac.uk/eidc/documents"));
    }

    /**
     * #71 was fixed by hardwiring {@code scheme("https")} for any host that was not localhost, because
     * the proxy could not be trusted to say. It can now (dri-one #260), so the scheme is read from the
     * request again - and a request that really is plain HTTP is answered honestly rather than
     * redirected to a scheme it never used. This is what fails if the hardwiring comes back.
     */
    @Test
    @DisplayName("does not invent HTTPS for a request that arrives without forwarded headers")
    @SneakyThrows
    void redirectDoesNotHardwireHttps() {
        //when
        mvc.perform(get("http://catalogue.ceh.ac.uk/"))
           .andExpect(redirectedUrl("http://catalogue.ceh.ac.uk/eidc/documents"));
    }

    @Test
    @DisplayName("preserves HTTP for local development so localhost is not forced to HTTPS")
    @SneakyThrows
    void redirectPreservesHttpForLocalhost() {
        //when
        mvc.perform(get("http://localhost:8080/"))
           .andExpect(redirectedUrl("http://localhost:8080/eidc/documents"));
    }
}
