package uk.ac.ceh.gateway.catalogue.wms;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;

/**
 * Filter to prevent WMS GetMap request 'format' query parameter being used
 * for content negotiation by ParameterContentNegotiationStrategy.
 *
 * Is specific to lowercase 'format', uppercase 'FORMAT' is not affected.
 */
@Slf4j
public class WmsFormatParameterFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        val wrappedRequest = new RequestWrapper(req);
        chain.doFilter(wrappedRequest, response);
    }

    @Slf4j
    public static class RequestWrapper extends HttpServletRequestWrapper {

        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            if (name.equals("format")) {
                log.debug(
                    "hidden 'format' parameter, actual value: {}",
                    super.getParameter(name)
                );
                return null;
            } else {
                return super.getParameter(name);
            }
        }

    }
}
