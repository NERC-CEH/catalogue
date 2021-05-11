package uk.ac.ceh.gateway.catalogue.config;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService;
import uk.ac.ceh.gateway.catalogue.services.*;
import uk.ac.ceh.gateway.catalogue.templateHelpers.GeminiExtractor;

import javax.annotation.PostConstruct;

@Configuration
@AllArgsConstructor
public class FreemarkerConfig {
    private final CatalogueService catalogueService;
    private final CodeLookupService codeLookupService;
    private final DownloadOrderDetailsService downloadOrderDetailsService;
    private final freemarker.template.Configuration freemarkerConfiguration;
    private final GeminiExtractor geminiExtractor;
    private final JenaLookupService jenaLookupService;
    private final MapServerDetailsService mapServerDetailsService;
    private final MetadataQualityService metadataQualityService;
    private final PermissionService permissionService;

    @SneakyThrows
    @PostConstruct
    public void configureFreemarkerSharedVariables() {
        freemarkerConfiguration.setSharedVariable("catalogues", catalogueService);
        freemarkerConfiguration.setSharedVariable("codes", codeLookupService);
        freemarkerConfiguration.setSharedVariable("downloadOrderDetails", downloadOrderDetailsService);
        freemarkerConfiguration.setSharedVariable("geminiHelper", geminiExtractor);
        freemarkerConfiguration.setSharedVariable("jena", jenaLookupService);
        freemarkerConfiguration.setSharedVariable("mapServerDetails", mapServerDetailsService);
        freemarkerConfiguration.setSharedVariable("metadataQuality", metadataQualityService);
        freemarkerConfiguration.setSharedVariable("permission", permissionService);
    }
}
