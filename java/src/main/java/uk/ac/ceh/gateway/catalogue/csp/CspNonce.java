package uk.ac.ceh.gateway.catalogue.csp;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * The current request's Content-Security-Policy nonce, shared with templates as {@code csp}: an inline
 * script runs only when it carries it, as {@code <script nonce="${csp.nonce}">}.
 */
public class CspNonce {

    /** The nonce {@link ContentSecurityPolicyFilter} set for this request, or "" outside one. */
    public String getNonce() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "";
        }
        Object nonce = attributes.getAttribute(ContentSecurityPolicyFilter.NONCE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return nonce == null ? "" : nonce.toString();
    }
}
