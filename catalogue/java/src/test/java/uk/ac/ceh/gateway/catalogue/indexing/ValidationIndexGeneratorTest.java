package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import org.junit.Test;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.validation.Validator;

/**
 *
 * @author cjohn
 */
public class ValidationIndexGeneratorTest {
    @Test
    public void checkThatValidationDelegatesToValidator() {
        //Given
        MetadataDocument toValidate = mock(MetadataDocument.class);
        Validator validator = mock(Validator.class, RETURNS_DEEP_STUBS);
        ValidationIndexGenerator generator = new ValidationIndexGenerator(Arrays.asList(validator));
        
        //When
        generator.generateIndex(toValidate);
        
        //Then
        verify(validator).validate(toValidate);
    }
}
