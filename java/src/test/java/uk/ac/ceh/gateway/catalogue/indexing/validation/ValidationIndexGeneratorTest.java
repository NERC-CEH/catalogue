package uk.ac.ceh.gateway.catalogue.indexing.validation;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.validation.ValidationResult;
import uk.ac.ceh.gateway.catalogue.validation.Validator;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Slf4j
class ValidationIndexGeneratorTest {
    @Test
    void checkThatValidationDelegatesToValidator() {
        //Given
        val toValidate = new GeminiDocument();
        toValidate.setId("1234");
        val validator = mock(Validator.class);
        given(validator.getName())
            .willReturn("test");
        given(validator.validate(toValidate))
            .willReturn(new ValidationResult());
        val generator = new ValidationIndexGenerator(List.of(validator));


        //When
        val validationReport = generator.generateIndex(toValidate);
        log.info(validationReport.toString());

        //Then
        verify(validator).validate(toValidate);
    }
}
