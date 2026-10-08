package uk.ac.ceh.gateway.catalogue.repository.checks;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoredVersion")
class StoredVersionTest {
    @Mock
    BundledReaderService<MetadataDocument> reader;

    @Test
    @DisplayName("reads nothing until a check asks for it")
    void lazy() {
        StoredVersion.of(reader, "cosmos");

        verifyNoInteractions(reader);
    }

    @Test
    @SneakyThrows
    @DisplayName("reads the document at most once, however often it is asked for")
    void readOnce() {
        GeminiDocument stored = new GeminiDocument();
        given(reader.readBundle("cosmos")).willReturn(stored);
        StoredVersion version = StoredVersion.of(reader, "cosmos");

        assertThat(version.get()).containsSame(stored);
        assertThat(version.get()).containsSame(stored);
        verify(reader, times(1)).readBundle("cosmos");
    }

    @Test
    @SneakyThrows
    @DisplayName("a document that does not exist is none, and is remembered as none")
    void absent() {
        given(reader.readBundle("cosmos")).willThrow(new GitFileNotFoundException("no such file"));
        StoredVersion version = StoredVersion.of(reader, "cosmos");

        assertThat(version.get()).isEmpty();
        assertThat(version.get()).isEmpty();
        verify(reader, times(1)).readBundle("cosmos");
    }

    @Test
    @SneakyThrows
    @DisplayName("a document that cannot be read is none, leaving the checks at their strictest")
    void unreadable() {
        given(reader.readBundle("cosmos")).willThrow(new IOException("pack file corrupt"));

        assertThat(StoredVersion.of(reader, "cosmos").get()).isEmpty();
    }

    @Test
    @DisplayName("not-found is recognised anywhere in an exception's causes")
    void notFoundInCauses() {
        assertThat(StoredVersion.isNotFound(new RuntimeException(new GitFileNotFoundException("x")))).isTrue();
        assertThat(StoredVersion.isNotFound(new RuntimeException(new IOException("x")))).isFalse();
    }
}
