package uk.ac.ceh.gateway.catalogue.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;

/**
 * The following is an XML schema validator which can validate XML document
 * representations against some schema.
 */
@Slf4j
public class XSDSchemaValidator extends AbstractDocumentValidator {
    private final Schema schema;

    public XSDSchemaValidator(String name, MediaType mediaType, DocumentWritingService documentWritingService, Schema schema) {
        super(name, mediaType, documentWritingService);
        this.schema = schema;
    }

    @Override
    public ValidationResult validate(InputStream stream) {
        ValidationResult validationResult = new ValidationResult();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(stream));

            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler(){
                @Override
                public void warning(SAXParseException ex) {
                    validationResult.reject(ex.getMessage(), ValidationLevel.WARNING);
                }

                @Override
                public void error(SAXParseException ex) {
                    validationResult.reject(ex.getMessage(), ValidationLevel.ERROR);
                }

                @Override
                public void fatalError(SAXParseException ex) {
                    validationResult.reject(ex.getMessage(), ValidationLevel.FAILED_TO_READ);
                }
            });
            validator.validate(new DOMSource(doc));
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            validationResult.reject(ex.getMessage(), ValidationLevel.FAILED_TO_READ);
        }
        return validationResult;
    }
}
