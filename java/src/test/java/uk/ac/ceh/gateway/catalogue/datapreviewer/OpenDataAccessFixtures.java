package uk.ac.ceh.gateway.catalogue.datapreviewer;

import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteRequestService;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Builds the real {@link OpenDataAccess} rule for unit tests, and marks a mocked record as released
 * (DOI under our prefix plus a data link), which the rule requires alongside the access fields.
 */
public final class OpenDataAccessFixtures {
    // Same as doi.prefix in application.properties, so Spring context tests can use released() too.
    public static final String DOI_PREFIX = "10.5285";

    private OpenDataAccessFixtures() {
    }

    public static OpenDataAccess openDataAccess() {
        DataciteService dataciteService = new DataciteService(
            "https://example.com/doi", DOI_PREFIX, "publisher", "legacy publisher", "username", "password",
            mock(DocumentIdentifierService.class), mock(RestTemplate.class),
            mock(JenaLookupService.class), mock(DataciteRequestService.class)
        );
        return new OpenDataAccess(dataciteService, downloadOrderDetailsService());
    }

    public static DownloadOrderDetailsService downloadOrderDetailsService() {
        DownloadUrlProperties properties = new DownloadUrlProperties();
        properties.setRegexOrder("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        properties.setRegexPackage("https://data-package\\.ceh\\.ac\\.uk/.*");
        properties.setRegexDatastore("https://catalogue\\.ceh\\.ac\\.uk/datastore/eidchub/.*");
        properties.setRegexCeda("https://data\\.ceda\\.ac\\.uk/eidc/.*");
        properties.setRegexSupportingDocs("https://data-package\\.ceh\\.ac\\.uk/sd/.*");
        properties.setRegexOrderManDownload("http(s?)://catalogue\\.ceh\\.ac\\.uk/download\\?fileIdentifier=.*");
        return new DownloadOrderDetailsService(properties);
    }

    public static ResourceIdentifier doi(String code) {
        return ResourceIdentifier.builder().codeSpace("doi").code(code).build();
    }

    public static OnlineResource link(String function, String url) {
        return OnlineResource.builder().function(function).url(url).build();
    }

    /** Stubs a mocked record with a DOI under our prefix and a fileAccess link. */
    public static void released(GeminiDocument document) {
        when(document.getResourceIdentifiers()).thenReturn(List.of(doi(DOI_PREFIX + "/abc123")));
        when(document.getOnlineResources()).thenReturn(
            List.of(link("fileAccess", "https://catalogue.ceh.ac.uk/datastore/eidchub/abc123")));
    }
}
