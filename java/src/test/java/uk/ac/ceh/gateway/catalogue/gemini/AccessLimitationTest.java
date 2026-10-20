package uk.ac.ceh.gateway.catalogue.gemini;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessLimitationTest {

    private static AccessLimitation withUri(String uri) {
        return AccessLimitation.builder().uri(uri).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // N07 "no limitations", which the editor saves since the June 2026 vocabulary change
        "http://vocab.nerc.ac.uk/collection/N07/current/UNRS/",
        // COAR "open access", which the editor saved before it
        "http://purl.org/coar/access_right/c_abf2"
    })
    void openAccessUrisGrantOpenAccess(String uri) {
        assertTrue(withUri(uri).grantsOpenAccess());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://vocab.nerc.ac.uk/collection/N07/current/RACC/",
        "http://vocab.nerc.ac.uk/collection/N07/current/RAUT/",
        "http://vocab.nerc.ac.uk/collection/N07/current/EMBG/",
        "http://vocab.nerc.ac.uk/collection/N07/current/PUNA/",
        ""
    })
    void restrictedOrMissingUrisDoNotGrantOpenAccess(String uri) {
        assertFalse(withUri(uri).grantsOpenAccess());
    }

    @Test
    void openAccessIsNotSerialisedIntoTheRecord() {
        String json = JsonMapper.builder().build().writeValueAsString(
            withUri("http://vocab.nerc.ac.uk/collection/N07/current/UNRS/"));

        assertFalse(json.contains("penAccess"), json);
    }
}
