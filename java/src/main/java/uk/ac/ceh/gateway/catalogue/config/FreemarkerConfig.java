package uk.ac.ceh.gateway.catalogue.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.quality.MultiDocumentTypeMetadataQualityService;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementQualityService;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.GeminiExtractor;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.userdetails.SecurityUserInfo;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;

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
    private final MultiDocumentTypeMetadataQualityService metadataQualityService;
    private final PermissionService permissionService;
    private final ProfileService profileService;
    @Nullable private final ServiceAgreementQualityService serviceAgreementQualityService;
    @Nullable private final MetricsService metricsService;

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
        freemarkerConfiguration.setSharedVariable("profile", profileService);
        freemarkerConfiguration.setSharedVariable("userInfo", new SecurityUserInfo());

        if (serviceAgreementQualityService != null) {
            freemarkerConfiguration.setSharedVariable("serviceAgreementQuality", serviceAgreementQualityService);
        }

        if (metricsService != null) {
            freemarkerConfiguration.setSharedVariable("metrics", metricsService);
        }
    }
}
