package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Renders a branded, status-aware error page in place of Spring Boot's default
 * Whitelabel error page.
 *
 * <p>This is registered as an {@link ErrorViewResolver}, which {@code BasicErrorController}
 * consults <em>before</em> falling back to the view literally named {@code error} (the
 * Whitelabel {@code StaticView} bean). We deliberately resolve to the view name
 * {@code html/error-page} — not {@code error} — so the FreeMarker view resolver renders
 * {@code templates/html/error-page.ftlh} rather than the Whitelabel bean.
 *
 * <p>A custom resolver is used instead of the more usual {@code error/4xx}/{@code error/5xx}
 * template convention because this application loads templates from a {@code file:} location,
 * which the {@code TemplateAvailabilityProvider} (used by {@code DefaultErrorViewResolver}
 * and {@code ErrorTemplateMissingCondition}) does not detect — so those mechanisms silently
 * fall through to Whitelabel. Resolving unconditionally here avoids that detection step.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomErrorViewResolver implements ErrorViewResolver {

    static final String ERROR_VIEW = "html/error-page";

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        log.debug("Rendering custom error page for status {} ({})", status.value(), model.get("path"));
        return new ModelAndView(ERROR_VIEW, model, status);
    }
}
