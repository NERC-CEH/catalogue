package uk.ac.ceh.gateway.catalogue.validation;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

import static java.util.stream.Stream.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.GEMINI_XML;
import static uk.ac.ceh.gateway.catalogue.validation.ValidationLevel.ERROR;
import static uk.ac.ceh.gateway.catalogue.validation.ValidationLevel.VALID;

@Slf4j
@ExtendWith(MockitoExtension.class)
class XSDSchemaValidatorTest {

    @Mock private DocumentWritingService writingService;
    private XSDSchemaValidator validator;
    private GeminiDocument geminiDocument;

    @BeforeEach
    void setup() {
        validator = new XSDSchemaValidator(
            "Gemini",
            GEMINI_XML,
            writingService,
            generateSchema()
        );
        geminiDocument = new GeminiDocument();
        geminiDocument.setTitle("Test");
    }

    @SneakyThrows
    private Schema generateSchema() {
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return schemaFactory.newSchema(
            of("gemini/srv/srv.xsd", "gemini/gmx/gmx.xsd")
                .map( (s) -> new StreamSource(new File("../schemas", s)))
                .toArray(Source[]::new)
        );
    }

    @SneakyThrows
    private void givenValidDocument() {
        val inputStream = getClass().getResourceAsStream("valid.xml");
        given(writingService.write(any(GeminiDocument.class), eq(GEMINI_XML)))
            .willReturn(inputStream);
    }

    @SneakyThrows
    private void givenInvalidDocument() {
        val inputStream = getClass().getResourceAsStream("invalid.xml");
        given(writingService.write(any(GeminiDocument.class), eq(GEMINI_XML)))
            .willReturn(inputStream);
    }

    @Test
    @SneakyThrows
    void validateValidDocument() {
        //given
        givenValidDocument();

        //when
        val result = validator.validate(geminiDocument);

        //then
        assertThat(result.getWorstLevel(), equalTo(VALID));
        assertThat(result.getMessages().size(), equalTo(0));
    }

    @Test
    @SneakyThrows
    void validateInvalidDocument() {
        //given
        givenInvalidDocument();

        //when
        val result = validator.validate(geminiDocument);

        //then
        assertThat(result.getWorstLevel(), equalTo(ERROR));
        assertThat(result.getMessages().size(), equalTo(1));
    }

}
