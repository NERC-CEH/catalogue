package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the suppression of PDFBox's font warnings, which Tika triggers once per font per file
 * while supporting documents are extracted for embedding. On staging they were 14,232 of 14,694
 * WARN lines, so a re-embedding run buried everything else in the log.
 *
 * <p>A property naming a package that no longer exists silences nothing and says nothing, so the
 * classes are referenced here directly: a PDFBox upgrade that moves them stops this compiling or
 * fails the assertion, rather than quietly restoring the flood.
 */
@DisplayName("PDFBox log suppression")
class PdfboxLogSuppressionTest {

    private static final String FONT_PACKAGE = "org.apache.pdfbox.pdmodel.font";

    private String shipped(String key) throws Exception {
        Properties properties = new Properties();
        try (InputStream in = new ClassPathResource("application.properties").getInputStream()) {
            properties.load(in);
        }
        return properties.getProperty(key);
    }

    @Test
    @DisplayName("the shipped configuration silences the font package")
    void fontPackageIsSilenced() throws Exception {
        assertThat(shipped("logging.level." + FONT_PACKAGE)).isEqualTo("error");
    }

    @Test
    @DisplayName("every class that emits the noise is still inside the silenced package")
    void theSilencedPackageStillCoversTheNoisyClasses() {
        assertThat(PDTrueTypeFont.class.getName()).startsWith(FONT_PACKAGE + ".");
        assertThat(PDType1Font.class.getName()).startsWith(FONT_PACKAGE + ".");
        assertThat(PDType0Font.class.getName()).startsWith(FONT_PACKAGE + ".");
        assertThat(PDSimpleFont.class.getName()).startsWith(FONT_PACKAGE + ".");
    }

    /**
     * Deliberately narrower than org.apache.pdfbox: the parser still reports genuinely malformed
     * PDFs, and that is worth reading.
     */
    @Test
    @DisplayName("PDFBox is not silenced wholesale, so parse failures still surface")
    void theParserIsNotSilenced() throws Exception {
        assertThat(shipped("logging.level.org.apache.pdfbox")).isNull();
    }
}
