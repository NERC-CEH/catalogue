package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FundingTest {

    @Test
    void isOrcid() {
        //given
        val funding = Funding.builder()
            .funderIdentifier("https://orcid.org/0000-0003-3541-5903")
            .build();

        //when

        //then
        assertTrue(funding.isOrcid());
    }

    @Test
    void isRor() {
        //given
        val funding = Funding.builder()
            .funderIdentifier("https://ror.org/abcdefghij")
            .build();

        //when

        //then
        assertTrue(funding.isRor());
    }
}
