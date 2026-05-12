package uk.ac.ceh.gateway.catalogue.metrics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@ToString
@Controller
public class DownloadController {
    private final MetricsService metricsService;
    private final List<String> excludedUsers;

    private final List<Pattern> validUrls;

    public DownloadController(
        @Nullable MetricsService metricsService,
        @Value("#{'${metrics.users.excluded}'.split(',')}") List<String> excludedUsers,
        @NotNull @Value("${download.url.regexOrder}") String orderUrlRegex,
        @NotNull @Value("${download.url.regexPackage}") String packageUrlRegex,
        @NotNull @Value("${download.url.regexDatastore}") String datastoreUrlRegex,
        @NotNull @Value("${download.url.regexCeda}") String cedaUrlRegex
    ) {
        this.metricsService = metricsService;
        this.excludedUsers = excludedUsers;
        this.validUrls = List.of(
            Pattern.compile(orderUrlRegex),
            Pattern.compile(packageUrlRegex),
            Pattern.compile(datastoreUrlRegex),
            Pattern.compile(cedaUrlRegex)
        );
        log.info("Creating");
    }

    @GetMapping("download/{uuid}")
    public String download(
        @ActiveUser CatalogueUser user,
        @PathVariable String uuid,
        @RequestParam("url") String url,
        HttpServletRequest request
    ) {
        String redirectUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        if(!valid(redirectUrl)) {
            throw new ResponseStatusException(HttpStatus.valueOf(404), "Invalid download url");
        }
        if(!excludedUsers.contains(user.getUsername()) && this.metricsService != null) {
            log.info(String.format("Redirecting to %s", redirectUrl));
            this.metricsService.recordDownload(uuid, request.getRemoteAddr());
        }
        return "redirect:" + redirectUrl;
    }

    protected boolean valid(String url){
        return this.validUrls
            .stream()
            .anyMatch(p -> p.matcher(url).matches());
    }
}
