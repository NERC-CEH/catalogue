package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AdminDeleteCsrfCustomizer} is shared, verbatim, by {@link SecurityConfig},
 * {@link SecurityConfigOidc} and {@link SecurityConfigCognito}. Rather than booting each of those three
 * profiles' full filter chains — the OIDC and Cognito ones need real OAuth2 client registrations and JWT
 * decoding infrastructure this test has no business depending on — this proves the shared customiser's
 * own behaviour once, in isolation, against a minimal security config built the same way all three
 * production chains build theirs. Because all three call the identical method, this is sufficient to
 * cover them: there is no per-profile logic left that could diverge.
 *
 * <p>The test context deliberately lists only the two nested classes below as sources (no component
 * scanning), so nothing from the real application — the datastore, Solr, Jena, or any of the other
 * {@code SecurityConfig*} classes — is pulled in.</p>
 */
@DisplayName("AdminDeleteCsrfCustomizer")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = {
        AdminDeleteCsrfCustomizerTest.SecurityConfigForTest.class,
        AdminDeleteCsrfCustomizerTest.EndpointsForTest.class
    }
)
class AdminDeleteCsrfCustomizerTest extends AbstractMvcTest {

    @Test
    @DisplayName("a POST to the admin delete route without a CSRF token is refused")
    void refusesTheAdminDeleteRouteWithoutAToken() throws Exception {
        mvc.perform(post("/maintenance/documents/delete/confirm"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a POST anywhere else still works without a CSRF token")
    void leavesEverywhereElseUnaffected() throws Exception {
        mvc.perform(post("/some/other/endpoint"))
            .andExpect(status().isOk());
    }

    @EnableAutoConfiguration
    @EnableWebSecurity
    static class SecurityConfigForTest {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AdminDeleteCsrfCustomizer::configure)
                .build();
        }
    }

    @RestController
    static class EndpointsForTest {
        @PostMapping("/maintenance/documents/delete/confirm")
        String confirm() {
            return "confirmed";
        }

        @PostMapping("/some/other/endpoint")
        String other() {
            return "ok";
        }
    }
}
