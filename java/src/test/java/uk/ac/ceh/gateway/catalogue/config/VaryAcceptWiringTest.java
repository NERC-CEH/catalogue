package uk.ac.ceh.gateway.catalogue.config;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies {@link VaryAcceptInterceptor} is actually registered with the MVC stack (dri-one #292).
 *
 * <p>{@link VaryAcceptInterceptorTest} covers the interceptor's behaviour; this covers the wiring,
 * which a unit test cannot. The handler chain is resolved without invoking the handler, so this needs
 * no collaborator mocks and stays independent of what any particular endpoint returns.
 */
@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("Vary: Accept wiring")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class VaryAcceptWiringTest {

    @Autowired private RequestMappingHandlerMapping handlerMapping;

    @Test
    @SneakyThrows
    @DisplayName("is applied to the content-negotiated search endpoint")
    void isAppliedToSearchEndpoint() {
        val chain = handlerMapping.getHandler(new MockHttpServletRequest("GET", "/documents"));

        assertThat(chain).isNotNull();
        assertThat(chain.getInterceptorList())
            .withFailMessage("VaryAcceptInterceptor is not registered for /documents")
            .anyMatch(VaryAcceptInterceptor.class::isInstance);
    }
}
