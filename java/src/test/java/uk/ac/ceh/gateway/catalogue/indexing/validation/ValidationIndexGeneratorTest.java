package uk.ac.ceh.gateway.catalogue.indexing.validation;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.validation.ValidationLevel;
import uk.ac.ceh.gateway.catalogue.validation.ValidationResult;
import uk.ac.ceh.gateway.catalogue.validation.Validator;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        //Then
        verify(validator).validate(toValidate);
        assertThat(validationReport.getDocumentId(), equalTo("1234"));
        assertTrue(validationReport.getResults().containsKey("test"));
        assertThat(validationReport.getResults().get("test"), equalTo(ValidationLevel.VALID));
    }
}
