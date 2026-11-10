package uk.ac.ceh.gateway.catalogue.datapreviewer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.ac.ceh.gateway.catalogue.datapreviewer.OpenDataAccessFixtures.DOI_PREFIX;
import static uk.ac.ceh.gateway.catalogue.datapreviewer.OpenDataAccessFixtures.doi;
import static uk.ac.ceh.gateway.catalogue.datapreviewer.OpenDataAccessFixtures.link;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenDataAccessTest {

    private static final String N07 = "http://vocab.nerc.ac.uk/collection/N07/current/";

    private final OpenDataAccess openDataAccess = OpenDataAccessFixtures.openDataAccess();

    private GeminiDocument document(boolean publicView, String availability, AccessLimitation limitation) {
        GeminiDocument document = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);
        Multimap<Permission, String> permissions = ArrayListMultimap.create();
        if (publicView) {
            permissions.put(Permission.VIEW, "public");
        }
        when(document.getMetadata()).thenReturn(metadata);
        when(metadata.getPermissions()).thenReturn(permissions);
        when(document.getAvailability()).thenReturn(availability);
        when(document.getAccessLimitation()).thenReturn(limitation);
        OpenDataAccessFixtures.released(document);
        return document;
    }

    /** A public, available, open-access record with the given release state. */
    private GeminiDocument openRecord(List<ResourceIdentifier> identifiers, List<OnlineResource> links) {
        GeminiDocument document = document(true, "Available", limitation("UNRS"));
        when(document.getResourceIdentifiers()).thenReturn(identifiers);
        when(document.getOnlineResources()).thenReturn(links);
        return document;
    }

    private static final OnlineResource FILE_ACCESS =
        link("fileAccess", "https://catalogue.ceh.ac.uk/datastore/eidchub/abc123");

    private AccessLimitation limitation(String code) {
        return new AccessLimitation("noLimitations", "", "", N07 + code + "/");
    }

    @Test
    void openWhenPublicAvailableAndNoLimitations() {
        assertThat(openDataAccess.isOpen(document(true, "Available", limitation("UNRS"))), is(true));
    }

    @Test
    void closedWhenRegistrationRequired() {
        assertThat(openDataAccess.isOpen(document(true, "Available", limitation("RACC"))), is(false));
    }

    @Test
    void closedWhenAvailabilityUnknown() {
        assertThat(openDataAccess.isOpen(document(true, "Unknown", limitation("UNRS"))), is(false));
    }

    @Test
    void closedWhenNotPublic() {
        assertThat(openDataAccess.isOpen(document(false, "Available", limitation("UNRS"))), is(false));
    }

    @Test
    void closedWhenNoAccessLimitation() {
        assertThat(openDataAccess.isOpen(document(true, "Available", null)), is(false));
    }

    @Test
    void closedWhenDocumentIsNull() {
        assertThat(openDataAccess.isOpen(null), is(false));
    }

    @Test
    void closedWhenDocumentHasNoMetadata() {
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getMetadata()).thenReturn(null);
        assertThat(openDataAccess.isOpen(document), is(false));
    }

    @Test
    void closedWhenPermissionsAreNull() {
        GeminiDocument document = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);
        when(document.getMetadata()).thenReturn(metadata);
        when(metadata.getPermissions()).thenReturn(null);
        assertThat(openDataAccess.isOpen(document), is(false));
    }

    @Test
    void closedWithoutDoi() {
        assertThat(openDataAccess.isOpen(openRecord(List.of(), List.of(FILE_ACCESS))), is(false));
    }

    @Test
    void closedWhenDoiIsNotUnderOurPrefix() {
        GeminiDocument document = openRecord(List.of(doi("10.1234/abc123")), List.of(FILE_ACCESS));
        assertThat(openDataAccess.isOpen(document), is(false));
    }

    @Test
    void closedWhenIdentifierIsNotADoi() {
        ResourceIdentifier ceh = ResourceIdentifier.builder().codeSpace("CEH:EIDC:").code("abc123").build();
        assertThat(openDataAccess.isOpen(openRecord(List.of(ceh), List.of(FILE_ACCESS))), is(false));
    }

    @Test
    void closedWithDoiButNoDataLinks() {
        GeminiDocument document = openRecord(List.of(doi(DOI_PREFIX + "/abc123")), List.of());
        assertThat(openDataAccess.isOpen(document), is(false));
    }

    @Test
    void closedWhenOnlyLinkIsSupportingDocumentation() {
        GeminiDocument document = openRecord(
            List.of(doi(DOI_PREFIX + "/abc123")),
            List.of(link("information", "https://data-package.ceh.ac.uk/sd/abc123.zip")));
        assertThat(openDataAccess.isOpen(document), is(false));
    }

    @Test
    void closedWhenOrderLinkIsNotTheOrderManager() {
        // An order link elsewhere marks an embargo on the record page, so it does not release the data.
        GeminiDocument document = openRecord(
            List.of(doi(DOI_PREFIX + "/abc123")),
            List.of(link("order", "https://example.com/embargoed")));
        assertThat(openDataAccess.isOpen(document), is(false));
    }

    @ParameterizedTest
    @CsvSource({
        "fileAccess, https://catalogue.ceh.ac.uk/datastore/eidchub/abc123",
        "download, https://data-package.ceh.ac.uk/data/abc123",
        "order, https://order-eidc.ceh.ac.uk/resources/ABCDEFGH/order"
    })
    void openWithDoiAndDataLink(String function, String url) {
        GeminiDocument document = openRecord(List.of(doi(DOI_PREFIX + "/abc123")), List.of(link(function, url)));
        assertThat(openDataAccess.isOpen(document), is(true));
    }
}
