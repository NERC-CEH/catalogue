package uk.ac.ceh.gateway.catalogue.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ceh.gateway.catalogue.validation.ValidationLevel.FAILED_TO_READ;
import static uk.ac.ceh.gateway.catalogue.validation.ValidationLevel.VALID;

public class ValidationLevelTest {
    @Test
    public void checkThatFailedIsMoreSevere() {
        //When
        boolean moreSevere = FAILED_TO_READ.isMoreSevere(VALID);

        //Then
        assertTrue(moreSevere);
    }

    @Test
    public void checkThatValidIsLessSevere() {
        //When
        boolean moreSevere = VALID.isMoreSevere(FAILED_TO_READ);

        //Then
        assertFalse(moreSevere);
    }

    @Test
    public void checkSameLevelIsNotMoreSevere() {
        //Given
        ValidationLevel valid = VALID;

        //When
        boolean moreSevere = valid.isMoreSevere(valid);

        //Then
        assertFalse(moreSevere);
    }
}
