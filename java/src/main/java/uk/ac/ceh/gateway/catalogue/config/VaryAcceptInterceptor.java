package uk.ac.ceh.gateway.catalogue.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Advertises {@code Vary: Accept} on responses produced by a controller method.
 *
 * <p>Many endpoints here serve entirely different representations from a single URL: a request to
 * {@code /documents} with {@code Accept: application/json} returns a JSON payload, while the same
 * URL with {@code Accept: text/html} returns a Freemarker-rendered page. Without {@code Vary: Accept}
 * a cache is licensed to store one representation and serve it for the other, so an HTML navigation
 * could be answered with a JSON body.
 *
 * <p>Only handler-method responses are annotated. Static resources are served by
 * {@code ResourceHttpRequestHandler} rather than a {@link HandlerMethod}, and their content does not
 * depend on {@code Accept}; adding the header there would fragment their cache entries for nothing.
 */
public class VaryAcceptInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Guarded because a FORWARD dispatch re-runs interceptors against the same response, which
        // would otherwise advertise Accept twice.
        if (handler instanceof HandlerMethod
            && !response.getHeaders(HttpHeaders.VARY).contains(HttpHeaders.ACCEPT)) {
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT);
        }
        return true;
    }
}
