package uk.ac.ceh.gateway.catalogue.people;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("NamedPerson")
class NamedPersonTest {

    private static ResponsibleParty party(String given, String family, String identifier) {
        return ResponsibleParty.builder()
            .givenName(given)
            .familyName(family)
            .organisationName("UK Centre for Ecology & Hydrology")
            .organisationIdentifier("https://ror.org/00pggkr55")
            .nameIdentifier(identifier)
            .build();
    }

    @Test
    @DisplayName("packs and unpacks as the same person")
    void roundTrip() {
        var person = new NamedPerson("0000-0002-1825-0097", "Josiah", "Carberry", "Brown University", "");
        assertThat(NamedPerson.unpack(person.pack()), equalTo(Optional.of(person)));
    }

    @Test
    @DisplayName("refuses a packed value without its five parts")
    void unpackRefusesOtherText() {
        assertThat(NamedPerson.unpack("0000-0002-1825-0097"), equalTo(Optional.empty()));
    }

    @Test
    @DisplayName("finds people with an ORCID in every contact field of a record, once each")
    void findsPeopleAcrossFields() {
        var document = new GeminiDocument();
        document.setAuthors(List.of(
            party("Clare", "Rowland", "https://orcid.org/0000-0002-0459-506X"),
            party("Ann", "Other", ""),
            party("Typo", "Digit", "https://orcid.org/0000-0002-1825-0098")
        ));
        document.setContributors(List.of(party("Josiah", "Carberry", "0000-0002-1825-0097")));
        document.setContactPoints(List.of(party("Clare", "Rowland", "http://orcid.org/0000-0002-0459-506X")));

        assertThat(NamedPerson.in(document), containsInAnyOrder(
            new NamedPerson("0000-0002-0459-506X", "Clare", "Rowland", "UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55"),
            new NamedPerson("0000-0002-1825-0097", "Josiah", "Carberry", "UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55")
        ));
    }

    @Test
    @DisplayName("finds people in a document type's own contact fields")
    void findsPeopleInOtherDocumentTypes() {
        var facility = new MonitoringFacility();
        facility.setPartners(List.of(party("Josiah", "Carberry", "https://orcid.org/0000-0002-1825-0097")));

        assertThat(NamedPerson.in(facility), contains(
            new NamedPerson("0000-0002-1825-0097", "Josiah", "Carberry", "UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55")
        ));
    }

    @Test
    @DisplayName("leaves out an iD with no name to offer it under")
    void needsAName() {
        var document = new GeminiDocument();
        document.setAuthors(List.of(party("", "", "https://orcid.org/0000-0002-1825-0097")));

        assertThat(NamedPerson.in(document), empty());
    }
}
