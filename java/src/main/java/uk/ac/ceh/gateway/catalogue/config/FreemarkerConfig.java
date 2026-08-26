package uk.ac.ceh.gateway.catalogue.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.quality.MultiDocumentTypeMetadataQualityService;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementQualityService;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.*;
import uk.ac.ceh.gateway.catalogue.userdetails.SecurityUserInfo;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;

@Configuration
@RequiredArgsConstructor
public class FreemarkerConfig {
    private final CatalogueService catalogueService;
    private final CodeLookupService codeLookupService;
    private final ContactUri contactUri;
    private final DownloadOrderDetailsService downloadOrderDetailsService;
    private final freemarker.template.Configuration freemarkerConfiguration;
    private final DownloadUrlProperties downloadUrlProperties;
    private final FormatUri formatUri;
    private final FundingUri fundingUri;
    private final GeminiExtractor geminiExtractor;
    private final JenaLookupService jenaLookupService;
    private final LicenceUri licenceUri;
    private final KeywordUri keywordUri;
    private final MapServerDetailsService mapServerDetailsService;
    private final MultiDocumentTypeMetadataQualityService metadataQualityService;
    private final PermissionService permissionService;
    private final ProfileService profileService;
    private final FileDetailsService fileDetailsService;
    private final FileListService fileListService;
    private final UriNormaliser uriNormaliser;
    @Nullable private final ServiceAgreementQualityService serviceAgreementQualityService;
    @Nullable private final MetricsService metricsService;
    @Value("${access-button.collection.link}") private String collectionAccessButtonLink;
    @Value("#{'${fuseki.catalogueIds:}'.split(',')}") private List<String> fusekiCatalogueIds;

    @SneakyThrows
    @PostConstruct
    public void configureFreemarkerSharedVariables() {
        freemarkerConfiguration.setSharedVariable("catalogues", catalogueService);
        freemarkerConfiguration.setSharedVariable("codes", codeLookupService);
        freemarkerConfiguration.setSharedVariable("contactUri", contactUri);
        freemarkerConfiguration.setSharedVariable("downloadOrderDetails", downloadOrderDetailsService);
        freemarkerConfiguration.setSharedVariable("formatUris", formatUri);
        freemarkerConfiguration.setSharedVariable("fundingUri", fundingUri);
        freemarkerConfiguration.setSharedVariable("geminiHelper", geminiExtractor);
        freemarkerConfiguration.setSharedVariable("jena", jenaLookupService);
        freemarkerConfiguration.setSharedVariable("licenceUris", licenceUri);
        freemarkerConfiguration.setSharedVariable("keywordUri", keywordUri);
        freemarkerConfiguration.setSharedVariable("mapServerDetails", mapServerDetailsService);
        freemarkerConfiguration.setSharedVariable("metadataQuality", metadataQualityService);
        freemarkerConfiguration.setSharedVariable("permission", permissionService);
        freemarkerConfiguration.setSharedVariable("profile", profileService);
        freemarkerConfiguration.setSharedVariable("uriNormaliser", uriNormaliser);
        freemarkerConfiguration.setSharedVariable("userInfo", new SecurityUserInfo());
        freemarkerConfiguration.setSharedVariable("fileDetails", fileDetailsService);
        freemarkerConfiguration.setSharedVariable("downloadUrlRegexes", downloadUrlProperties);
        freemarkerConfiguration.setSharedVariable("fileListService", fileListService);
        freemarkerConfiguration.setSharedVariable("collectionAccessButtonLink", collectionAccessButtonLink);
        freemarkerConfiguration.setSharedVariable("voidCatalogueIds",
            fusekiCatalogueIds.stream().filter(s -> !s.isBlank()).collect(Collectors.toList()));

        if (serviceAgreementQualityService != null) {
            freemarkerConfiguration.setSharedVariable("serviceAgreementQuality", serviceAgreementQualityService);
        }

        if (metricsService != null) {
            freemarkerConfiguration.setSharedVariable("metrics", metricsService);
        }
    }
}
