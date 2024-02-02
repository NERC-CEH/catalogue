package uk.ac.ceh.gateway.catalogue.model;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponsiblePartyTest {

    @Test
    void isOrcid() {
        //given
        val responsibleParty = ResponsibleParty.builder()
            .nameIdentifier("https://orcid.org/0000-0003-3541-5903")
            .build();

        //when

        //then
        assertTrue(responsibleParty.isOrcid());
    }

    @Test
    void isNotOrcid() {
        //given
        val responsibleParty = ResponsibleParty.builder()
            .nameIdentifier("https://example.com/0000-0003-3541-5903")
            .build();

        //when

        //then
        assertFalse(responsibleParty.isOrcid());
    }

    @Test
    void isIsni() {
        //given
        val responsibleParty = ResponsibleParty.builder()
            .nameIdentifier("https://isni.org/isni/000000011850060X")
            .build();

        //when

        //then
        assertTrue(responsibleParty.isIsni());
    }

    @Test
    void isNotIsni() {
        //given
        val responsibleParty = ResponsibleParty.builder()
            .nameIdentifier("https://example.org/bob")
            .build();

        //when

        //then
        assertFalse(responsibleParty.isIsni());
    }

    @Test
    void isRor() {
        //given
        val responsibleParty = ResponsibleParty.builder()
            .organisationIdentifier("https://ror.org/abcdefghij")
            .build();

        //when

        //then
        assertTrue(responsibleParty.isRor());
    }

    @Test
    void isNotRor() {
        //given
        // TODO: regex allows numbers, I don't understand
        val responsibleParty = ResponsibleParty.builder()
            .organisationIdentifier("https://ror.org/a123456789www")
            .build();

        //when

        //then
        assertFalse(responsibleParty.isRor());
    }

    @Test
    void isNotRorAsOrganisationIdentifierNull() {
        //given
        val responsibleParty = ResponsibleParty.builder()
            .build();

        //when

        //then
        assertFalse(responsibleParty.isRor());
    }
}
