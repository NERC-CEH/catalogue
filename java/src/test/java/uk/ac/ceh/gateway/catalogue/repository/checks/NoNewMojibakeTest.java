package uk.ac.ceh.gateway.catalogue.repository.checks;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MojibakeTextException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * dri-one #328. Each case saves a document as {@code tulips}; a case about what was already
 * stored stubs the stored version, and every other one is a create.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NoNewMojibake")
class NoNewMojibakeTest {
    private static final CatalogueUser USER = new CatalogueUser("test", "test@example.com");

    // A real (not mocked) mapper: the guard scans the document's actual serialised form, so the
    // test needs genuine JSON output rather than a stubbed one.
    private final NoNewMojibake noNewMojibake = new NoNewMojibake(JsonMapper.builder().build());

    @Mock
    BundledReaderService<MetadataDocument> reader;

    private void check(MetadataDocument document) {
        noNewMojibake.check(USER, document, "tulips", StoredVersion.of(reader, "tulips"));
    }

    private static GeminiDocument withCopyright(String... notices) {
        return (GeminiDocument) new GeminiDocument().setUseConstraints(Arrays.stream(notices)
            .map(notice -> ResourceConstraint.builder().code("copyright").value(notice).build())
            .toList());
    }

    @Test
    @DisplayName("refuses double-encoded text in a new record")
    void newMojibake() {
        // A copyright notice already double-encoded: "©" (U+00A9) mis-decoded via CP1252 into
        // "Â©" (U+00C2 U+00A9) - the dri-one #328 signature.
        assertThrows(MojibakeTextException.class, () -> check(withCopyright("Â© 2020 UKCEH, some rights reserved")));
    }

    @Test
    @SneakyThrows
    @DisplayName("allows a record that already contained it, so an unrelated fix is not blocked")
    void alreadyStored() {
        // The corruption predates the guard: only newly introduced matches are refused.
        given(reader.readBundle("tulips")).willReturn(withCopyright("Â© 2020 UKCEH"));
        GeminiDocument incoming = withCopyright("Â© 2020 UKCEH");
        incoming.setTitle("A corrected title");

        assertDoesNotThrow(() -> check(incoming));
    }

    @Test
    @SneakyThrows
    @DisplayName("refuses more of it added to a record that already contained some")
    void moreOfTheSame() {
        // Counting occurrences, not just comparing the set: a second Â© pasted somewhere else is
        // still new corruption even though that sequence was already in the record.
        given(reader.readBundle("tulips")).willReturn(withCopyright("Â© 2020 UKCEH"));

        assertThrows(MojibakeTextException.class,
            () -> check(withCopyright("Â© 2020 UKCEH", "Also Â© someone else")));
    }

    @Test
    @DisplayName("allows a capital A with circumflex followed by a letter")
    void circumflexBeforeALetter() {
        // "Â" followed by a letter is ordinary text in several languages - Vietnamese "Ân",
        // upper-cased Romanian "CÂMPINA", Welsh "TÂN" - and is plausible in a name or a place
        // keyword. Real mojibake is "Â" standing in for punctuation or a symbol.
        GeminiDocument document = new GeminiDocument();
        document.setTitle("Soil survey of CÂMPINA and TÂN districts");

        assertDoesNotThrow(() -> check(document));
    }

    @Test
    @DisplayName("allows a capital A with circumflex followed by a space")
    void circumflexBeforeASpace() {
        assertDoesNotThrow(() -> check(withCopyright("Field strength is measured in Â mT and is freely available")));
    }

    @Test
    @DisplayName("does not read the stored version of a document with nothing to compare")
    void cleanDocumentReadsNothing() {
        GeminiDocument document = new GeminiDocument();
        document.setTitle("Soil moisture at Morley");

        check(document);

        verifyNoInteractions(reader);
    }
}
