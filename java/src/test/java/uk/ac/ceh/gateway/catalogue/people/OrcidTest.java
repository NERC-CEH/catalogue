package uk.ac.ceh.gateway.catalogue.people;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Orcid")
class OrcidTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "0000-0002-1825-0097",
        "https://orcid.org/0000-0002-1825-0097",
        "http://orcid.org/0000-0002-1825-0097",
        "https://www.orcid.org/0000-0002-1825-0097/",
        "orcid.org/0000-0002-1825-0097",
        "  0000-0002-1825-0097  "
    })
    @DisplayName("reads an iD however it is typed")
    void readsAnyForm(String value) {
        assertThat(Orcid.id(value), equalTo(Optional.of("0000-0002-1825-0097")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0000-0002-1694-233X", "0000-0002-1694-233x", "https://orcid.org/0000-0002-1694-233x"})
    @DisplayName("upper-cases a check character of ten")
    void checkCharacterX(String value) {
        assertThat(Orcid.id(value), equalTo(Optional.of("0000-0002-1694-233X")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "0000-0002-1825-0098",          // wrong check character
        "0000-0002-1852-0097",          // transposed digits
        "0000-0002-1825-009",           // too short
        "https://orcid.org/0000-0002-1825-0097?x",
        "https://example.org/0000-0002-1825-0097",
        "https://isni.org/isni/000000012146438X",
        "Clare Rowland"
    })
    @DisplayName("refuses anything that isn't a real iD")
    void refuses(String value) {
        assertThat(Orcid.id(value), equalTo(Optional.empty()));
    }
}
