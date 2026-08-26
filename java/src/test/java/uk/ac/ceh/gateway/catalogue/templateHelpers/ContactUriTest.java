package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;

@DisplayName("Identifying the RDF node for a contact")
class ContactUriTest {

    private ContactUri service;

    @BeforeEach
    void setUp() {
        service = new ContactUri(new UriNormaliser());
    }

    private static ResponsibleParty person(String familyName, String givenName) {
        return ResponsibleParty.builder().familyName(familyName).givenName(givenName).build();
    }

    private static ResponsibleParty named(String displayName) {
        return ResponsibleParty.builder().displayName(displayName).build();
    }

    @Nested
    @DisplayName("Preferring a persistent identifier the depositor supplied")
    class PersistentIdentifiers {

        @Test
        @DisplayName("an ORCID identifies the person, canonicalised to https")
        void orcid() {
            val contact = ResponsibleParty.builder()
                .familyName("Wood")
                .givenName("Claire")
                .nameIdentifier("http://orcid.org/0000-0003-4649-0677")
                .build();

            assertThat(
                service.identify(contact, "rec", "a", 0),
                is(equalTo("<https://orcid.org/0000-0003-4649-0677>"))
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://isni.org/isni/0000000121032683",
            "http://isni.org/isni/0000000121032683"
        })
        @DisplayName("an ISNI identifies the person, which dri-one #319 found was being discarded")
        void isni(String supplied) {
            val contact = ResponsibleParty.builder()
                .familyName("Wood")
                .givenName("Claire")
                .nameIdentifier(supplied)
                .build();

            assertThat(
                service.identify(contact, "rec", "a", 0),
                is(equalTo("<https://isni.org/isni/0000000121032683>"))
            );
        }

        @Test
        @DisplayName("a nameIdentifier we cannot recognise leaves the person on their name-derived node")
        void unrecognisedNameIdentifier() {
            val contact = ResponsibleParty.builder()
                .familyName("Wood")
                .givenName("Claire")
                .nameIdentifier("Claire Wood, UKCEH")
                .build();

            assertThat(service.identify(contact, "rec", "a", 0), is(equalTo(service.identify(person("Wood", "Claire"), "other", "c", 3))));
        }
    }

    @Nested
    @DisplayName("Minting a stable node for a person with no persistent identifier")
    class StablePersonNodes {

        @Test
        @DisplayName("the node is a legal Turtle prefixed name in the catalogue's id namespace")
        void shape() {
            assertThat(
                service.identify(person("Wood", "Claire"), "rec", "a", 0),
                matchesRegex(":person_[0-9a-f]{16}")
            );
        }

        @Test
        @DisplayName("the same person is one node across records, which is the point of #319")
        void sameAcrossRecords() {
            assertThat(
                service.identify(person("Wood", "Claire"), "recordOne", "a", 0),
                is(equalTo(service.identify(person("Wood", "Claire"), "recordTwo", "a", 0)))
            );
        }

        @Test
        @DisplayName("moving up the author list no longer changes who the person is")
        void independentOfPosition() {
            assertThat(
                service.identify(person("Wood", "Claire"), "rec", "a", 17),
                is(equalTo(service.identify(person("Wood", "Claire"), "rec", "a", 0)))
            );
        }

        @Test
        @DisplayName("an author and a contact point spelled the same are the same person")
        void independentOfRole() {
            assertThat(
                service.identify(person("Wood", "Claire"), "rec", "a", 0),
                is(equalTo(service.identify(person("Wood", "Claire"), "rec", "c", 0)))
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"Wood, C.M.", "Wood, C. M.", "Wood, C M", "wood, c.m.", "Wood,C.M."})
        @DisplayName("punctuation, spacing and case in a name do not fork the person")
        void insignificantSpelling(String displayName) {
            assertThat(
                service.identify(named(displayName), "rec", "a", 0),
                is(equalTo(service.identify(named("Wood, C.M."), "rec", "a", 0)))
            );
        }

        @Test
        @DisplayName("an accent typed one way in one record and another in the next is one person")
        void accentsFolded() {
            assertThat(
                service.identify(named("Edwards, François"), "rec", "a", 0),
                is(equalTo(service.identify(named("Edwards, Francois"), "rec", "a", 0)))
            );
        }

        @Test
        @DisplayName("a structured name and the same name typed as a display name agree")
        void structuredMatchesDisplayName() {
            assertThat(
                service.identify(person("Wood", "Claire"), "rec", "a", 0),
                is(equalTo(service.identify(named("Wood, C."), "rec", "a", 0)))
            );
        }

        @Test
        @DisplayName("different people keep different nodes")
        void differentPeople() {
            assertThat(
                service.identify(person("Wood", "Claire"), "rec", "a", 0),
                is(not(equalTo(service.identify(person("Wood", "David"), "rec", "a", 0))))
            );
        }

        @Test
        @DisplayName("a spelled-out given name is not merged with its own initial, as #319 requires")
        void spelledOutGivenNameIsNotAnInitial() {
            assertThat(
                service.identify(named("Wood, Claire"), "rec", "a", 0),
                is(not(equalTo(service.identify(named("Wood, C."), "rec", "a", 0))))
            );
        }

        @Test
        @DisplayName("the organisation a person gives is not part of who they are")
        void organisationDoesNotFork() {
            val ceh = ResponsibleParty.builder()
                .familyName("Wood").givenName("Claire")
                .organisationName("Centre for Ecology & Hydrology")
                .build();
            val ukceh = ResponsibleParty.builder()
                .familyName("Wood").givenName("Claire")
                .organisationName("UK Centre for Ecology & Hydrology")
                .build();

            assertThat(service.identify(ceh, "rec", "a", 0), is(equalTo(service.identify(ukceh, "rec", "a", 0))));
        }
    }

    @Nested
    @DisplayName("Identifying an organisation")
    class Organisations {

        @Test
        @DisplayName("a ROR identifies the organisation")
        void ror() {
            val organisation = ResponsibleParty.builder()
                .organisationName("UK Centre for Ecology & Hydrology")
                .organisationIdentifier("https://ror.org/00pggkr55")
                .build();

            assertThat(service.identify(organisation, "rec", "c", 0), is(equalTo("<https://ror.org/00pggkr55>")));
        }

        @Test
        @DisplayName("without a ROR an organisation stays on its record-scoped node")
        void withoutRor() {
            val organisation = ResponsibleParty.builder()
                .organisationName("UK Centre for Ecology & Hydrology")
                .build();

            assertThat(service.identify(organisation, "rec", "c", 2), is(equalTo(":rec_c2")));
        }
    }

    @Nested
    @DisplayName("Falling back to the record-scoped node")
    class Fallback {

        @Test
        @DisplayName("a contact with nothing to identify it at all")
        void empty() {
            assertThat(
                service.identify(ResponsibleParty.builder().build(), "rec", "pub", 1),
                is(equalTo(":rec_pub1"))
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"---", ".", "?"})
        @DisplayName("a name with no letters or digits in it cannot identify anybody")
        void unusableName(String displayName) {
            assertThat(service.identify(named(displayName), "rec", "a", 4), is(equalTo(":rec_a4")));
        }
    }

    @Nested
    @DisplayName("Identifying an affiliation with no ROR (dri-one #334)")
    class Affiliations {

        private static ResponsibleParty at(String organisationName) {
            return ResponsibleParty.builder()
                .familyName("Wood").givenName("Claire")
                .organisationName(organisationName)
                .build();
        }

        @Test
        @DisplayName("an organisation is identified by its name, and recognisable as minted")
        void organisationMints() {
            assertThat(
                service.identifyOrganisation(at("University of Exeter")),
                matchesRegex(":organisation_[0-9a-f]{16}")
            );
        }

        @Test
        @DisplayName("the same organisation on two contacts is one node")
        void sameOrganisationOneNode() {
            assertThat(
                service.identifyOrganisation(at("University of Exeter")),
                is(service.identifyOrganisation(at("university of exeter")))
            );
        }

        @ParameterizedTest
        @DisplayName("punctuation, spacing and accents do not fork an organisation")
        @ValueSource(strings = {
            "UK Centre for Ecology & Hydrology",
            "UK Centre for Ecology and Hydrology",
            "The University of Edinburgh",
            "Institute of Terrestrial Ecology"
        })
        void differentInstitutionsStayDifferent(String name) {
            assertThat(
                "only spelling-identical names converge; the rest is data cleanup",
                service.identifyOrganisation(at(name)),
                not(equalTo(service.identifyOrganisation(at("University of Edinburgh"))))
            );
        }

        @Test
        @DisplayName("an ampersand written out is a different organisation, deliberately")
        void ampersandIsNotNormalised() {
            assertThat(
                service.identifyOrganisation(at("UK Centre for Ecology & Hydrology")),
                not(equalTo(service.identifyOrganisation(at("UK Centre for Ecology and Hydrology"))))
            );
        }

        @Test
        @DisplayName("a contact naming no organisation gets no node, rather than an empty one")
        void noOrganisation() {
            assertThat(service.identifyOrganisation(person("Wood", "Claire")), is(""));
            assertThat(service.identifyOrganisation(at("   ")), is(""));
        }

        @Test
        @DisplayName("an organisation and a person sharing a name do not collide")
        void prefixesKeepNodeTypesApart() {
            assertThat(
                service.identifyOrganisation(at("Wood")),
                not(equalTo(service.identify(named("Wood"), "rec", "a", 0)))
            );
        }
    }

    @Nested
    @DisplayName("Identifying a role held on a record (dri-one #334)")
    class Roles {

        @Test
        @DisplayName("the role node is recognisable as minted")
        void roleMints() {
            assertThat(
                service.identifyRole(":person_abc", "pro:author", "record1"),
                matchesRegex(":role_[0-9a-f]{16}")
            );
        }

        @Test
        @DisplayName("the same person, role and record always give the same node")
        void stable() {
            assertThat(
                service.identifyRole(":person_abc", "pro:author", "record1"),
                is(service.identifyRole(":person_abc", "pro:author", "record1"))
            );
        }

        @Test
        @DisplayName("a role is scoped to its record, so the same role elsewhere is a different node")
        void scopedToTheRecord() {
            assertThat(
                service.identifyRole(":person_abc", "pro:author", "record1"),
                not(equalTo(service.identifyRole(":person_abc", "pro:author", "record2")))
            );
        }

        @Test
        @DisplayName("changing the person or the role changes the node")
        void everyPartOfTheKeyCounts() {
            val base = service.identifyRole(":person_abc", "pro:author", "record1");
            assertThat(base, not(equalTo(service.identifyRole(":person_xyz", "pro:author", "record1"))));
            assertThat(base, not(equalTo(service.identifyRole(":person_abc", "scoro:data-curator", "record1"))));
        }

        @Test
        @DisplayName("the key parts cannot run together, so a shifted boundary is a different node")
        void keyPartsCannotRunTogether() {
            assertThat(
                service.identifyRole(":person_ab", "cpro:author", "record1"),
                not(equalTo(service.identifyRole(":person_abc", "pro:author", "record1")))
            );
        }
    }
}
