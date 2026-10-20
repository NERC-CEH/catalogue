package uk.ac.ceh.gateway.catalogue.datapreviewer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenDataAccessTest {

    private static final String N07 = "http://vocab.nerc.ac.uk/collection/N07/current/";

    private final OpenDataAccess openDataAccess = new OpenDataAccess();

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
        return document;
    }

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
}
