package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;

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

    private final List<Pattern> validUrls = List.of(
        Pattern.compile("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*"),
        Pattern.compile("https://data-package\\.ceh\\.ac\\.uk/.*"),
        Pattern.compile("https://catalogue\\.ceh\\.ac\\.uk/datastore/eidchub/.*")
    );

    public DownloadController(
        @Nullable MetricsService metricsService,
        @Value("#{'${metrics.users.excluded}'.split(',')}") List<String> excludedUsers
    ) {
        this.metricsService = metricsService;
        this.excludedUsers = excludedUsers;
        log.info("Creating {}", this);
    }

    @GetMapping("download/{uuid}")
    public String download(
        @ActiveUser CatalogueUser user,
        @PathVariable("uuid") String uuid,
        @RequestParam("url") String url,
        HttpServletRequest request
    ) {
        String redirectUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        if(!valid(redirectUrl)) {
            throw new RuntimeException("Invalid download url");
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
