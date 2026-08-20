package uk.ac.ceh.gateway.catalogue.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link VaryAcceptInterceptor} (dri-one #292).
 *
 * <p>Endpoints such as {@code /documents} serve JSON or a rendered HTML page from one URL depending
 * purely on the {@code Accept} header, so the response has to advertise that it varies by it.
 */
@DisplayName("VaryAcceptInterceptor")
class VaryAcceptInterceptorTest {

    private final VaryAcceptInterceptor interceptor = new VaryAcceptInterceptor();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @SneakyThrows
    private HandlerMethod handlerMethod() {
        return new HandlerMethod(new Object(), Object.class.getMethod("toString"));
    }

    @Test
    @DisplayName("advertises Vary: Accept for a controller method")
    void advertisesVaryAcceptForHandlerMethod() {
        interceptor.preHandle(request, response, handlerMethod());

        assertThat(response.getHeaders("Vary")).containsExactly("Accept");
    }

    @Test
    @DisplayName("leaves static resource responses alone")
    void leavesStaticResourcesAlone() {
        // Static resources are served by ResourceHttpRequestHandler, not a HandlerMethod, and their
        // content does not depend on Accept. Advertising it would fragment their cache entries.
        interceptor.preHandle(request, response, new ResourceHttpRequestHandler());

        assertThat(response.getHeaders("Vary")).isEmpty();
    }

    @Test
    @DisplayName("does not duplicate Accept when interceptors re-run on a forward")
    void doesNotDuplicateOnForward() {
        interceptor.preHandle(request, response, handlerMethod());
        interceptor.preHandle(request, response, handlerMethod());

        assertThat(response.getHeaders("Vary")).containsExactly("Accept");
    }

    @Test
    @DisplayName("preserves Vary values contributed by other filters")
    void preservesExistingVaryValues() {
        // Spring Security's CORS support already contributes Origin and the
        // Access-Control-Request-* values; those must survive.
        response.addHeader("Vary", "Origin");

        interceptor.preHandle(request, response, handlerMethod());

        assertThat(response.getHeaders("Vary")).containsExactly("Origin", "Accept");
    }

    @Test
    @DisplayName("always continues the handler chain")
    void alwaysContinuesTheChain() {
        assertThat(interceptor.preHandle(request, response, handlerMethod())).isTrue();
        assertThat(interceptor.preHandle(request, response, new ResourceHttpRequestHandler())).isTrue();
    }
}
