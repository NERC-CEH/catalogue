package uk.ac.ceh.gateway.catalogue.repository.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.ResourceIdentifierExistsException;
import uk.ac.ceh.gateway.catalogue.services.ResourceIdentifierLookupService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("UniqueResourceIdentifiers")
class UniqueResourceIdentifiersTest {
    private static final CatalogueUser USER = new CatalogueUser("test", "test@example.com");

    // The stored version is passed as null throughout: uniqueness never looks at it
    @Mock
    ResourceIdentifierLookupService lookup;

    private GeminiDocument withIdentifier(String codeSpace, String code) {
        return (GeminiDocument) new GeminiDocument().setResourceIdentifiers(List.of(
            ResourceIdentifier.builder().codeSpace(codeSpace).code(code).build()
        ));
    }

    @Test
    @DisplayName("refuses an identifier another document holds")
    void duplicate() {
        given(lookup.findDocumentIdsByRi("ukceh.eidc:fafa99")).willReturn(List.of("existing-doc"));

        assertThrows(ResourceIdentifierExistsException.class, () ->
            new UniqueResourceIdentifiers(lookup).check(USER, withIdentifier("ukceh.eidc", "fafa99"), "tulips", null));
    }

    @Test
    @DisplayName("allows an identifier only the document being saved holds")
    void ownIdentifier() {
        given(lookup.findDocumentIdsByRi("ukceh.eidc:fafa99")).willReturn(List.of("tulips"));

        assertDoesNotThrow(() ->
            new UniqueResourceIdentifiers(lookup).check(USER, withIdentifier("ukceh.eidc", "fafa99"), "tulips", null));
    }

    @Test
    @DisplayName("ignores an identifier with no code space, such as the record's own URI")
    void noCodeSpace() {
        assertDoesNotThrow(() ->
            new UniqueResourceIdentifiers(lookup).check(USER, withIdentifier(null, "https://example.com/id/tulips"), "tulips", null));
        verifyNoInteractions(lookup);
    }
}
