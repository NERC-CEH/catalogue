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

    @Test
    public void testGetFullNameWithFamilyAndGivenName() {
        ResponsibleParty party = ResponsibleParty.builder()
            .familyName("Doe")
            .givenName("john")
            .build();
        assertEquals("Doe, J.", party.getFullName());
    }

    @Test
    public void testGetFullNameWithFamilyAndMultipleGivenNames() {
        ResponsibleParty party = ResponsibleParty.builder()
            .familyName("Doe")
            .givenName("John Paul")
            .build();
        assertEquals("Doe, J.P.", party.getFullName());
    }

    @Test
    public void testGetFullNameWithIndividualName() {
        ResponsibleParty party = ResponsibleParty.builder()
            .individualName("John, D.")
            .build();
        assertEquals("John, D.", party.getFullName());
    }

    @Test
    public void testGetFullNameWithEmptyNames() {
        ResponsibleParty party = ResponsibleParty.builder().build();
        assertEquals("", party.getFullName());
    }

    @Test
    public void testGetFullNameWithOnlyFamilyName() {
        ResponsibleParty party = ResponsibleParty.builder()
            .familyName("Doe")
            .build();
        assertEquals("", party.getFullName());
    }

    @Test
    public void testGetFullNameWithOnlyGivenName() {
        ResponsibleParty party = ResponsibleParty.builder()
            .givenName("John")
            .build();
        assertEquals("", party.getFullName());
    }

    @Test
    public void testGetFullNameWithMultipleInitials() {
        ResponsibleParty party = ResponsibleParty.builder()
            .familyName("Foo")
            .givenName("J.P.")
            .build();
        assertEquals("Foo, J.P.", party.getFullName());
    }
}
