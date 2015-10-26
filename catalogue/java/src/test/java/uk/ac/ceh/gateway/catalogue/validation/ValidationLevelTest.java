package uk.ac.ceh.gateway.catalogue.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static uk.ac.ceh.gateway.catalogue.validation.ValidationLevel.FAILED_TO_READ;
import static uk.ac.ceh.gateway.catalogue.validation.ValidationLevel.VALID;

/**
 *
 * @author cjohn
 */
public class ValidationLevelTest {
    @Test
    public void checkThatFailedIsMoreSevere() {
        //Given
        ValidationLevel valid = VALID;
        ValidationLevel failed = FAILED_TO_READ;
        
        //When
        boolean moreSevere = ValidationLevel.isMoreSevere(valid, failed);
        
        //Then
        assertTrue("Expected failed to be more severe", moreSevere);
    }
    
    @Test
    public void checkThatValidIsLessSevere() {
        //Given
        ValidationLevel valid = VALID;
        ValidationLevel failed = FAILED_TO_READ;
        
        //When
        boolean moreSevere = ValidationLevel.isMoreSevere(failed, valid);
        
        //Then
        assertFalse("Expected failed to be less severe", moreSevere);
    }
    
    @Test
    public void checkSameLevelIsNotMoreSevere() {
        //Given
        ValidationLevel valid = VALID;
        
        //When
        boolean moreSevere = ValidationLevel.isMoreSevere(valid, valid);
        
        //Then
        assertFalse("Expected failed to be less severe", moreSevere);
    }
}
