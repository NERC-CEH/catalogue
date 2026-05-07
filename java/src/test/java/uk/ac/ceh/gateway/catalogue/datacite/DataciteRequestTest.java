package uk.ac.ceh.gateway.catalogue.datacite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.*;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteRequest.Attributes.*;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class DataciteRequestTest {

    DataciteRequestService attributes;
    GeminiDocument docMock;

    @BeforeEach
    void setUp() {
        docMock = mock(GeminiDocument.class);
        attributes = new DataciteRequestService();
    }

    @Test
    void testFundingDetails() {
        Funding rorFunder = Funding.builder()
            .funderName("UKRI")
            .funderIdentifier("https://ror.org/016476m91")
            .awardTitle("title")
            .awardNumber("1")
            .awardURI("Award123")
            .build();

        Funding orcidFunder = Funding.builder()
            .funderName("NERC")
            .funderIdentifier("https://orcid.org/0000-0003-3541-5903")
            .awardTitle("title2")
            .awardNumber("2")
            .awardURI("Award456")
            .build();

        Funding otherFunder = Funding.builder()
            .funderName("BGS")
            .funderIdentifier("https://example.org/funder")
            .awardTitle("title3")
            .awardNumber("3")
            .awardURI("Award789")
            .build();


        List<DataciteRequest.Attributes.FundingReference> result = attributes.fundingDetails(List.of(rorFunder, orcidFunder, otherFunder));

        assertEquals(3, result.size());
        assertEquals("ROR", result.get(0).funderIdentifierType());
        assertEquals("Crossref Funder", result.get(1).funderIdentifierType());
        assertEquals("Other", result.get(2).funderIdentifierType());
    }

    @Test
    void testExtractGeoLocations() {
        BoundingBox box = BoundingBox.builder().westBoundLongitude("-5.0").eastBoundLongitude("1.0").southBoundLatitude("50.0").northBoundLatitude("60.0").build();
        List<DataciteRequest.Attributes.GeoLocation> geoLocations = attributes.extractGeoLocations(List.of(box));
        assertEquals(1, geoLocations.size());
        assertNotNull(geoLocations.getFirst().geoLocationBox());
    }

    @Test
    void testPopulateDescriptionsLineage() {
        when(docMock.getDescription()).thenReturn("verbs, adverbs and adjectives");
        when(docMock.getLineage()).thenReturn("Mr Des Cription");
        List<Description> descriptions = attributes.populateDescriptions(docMock);
        assertEquals(2, descriptions.size());
        assertEquals("Methods", descriptions.get(1).descriptionType());
    }

    @Test
    void testPopulateDescriptionsEmptyLin() {
        when(docMock.getDescription()).thenReturn("verbs, adverbs and adjectives");
        when(docMock.getLineage()).thenReturn("");
        List<Description> descriptions = attributes.populateDescriptions(docMock);
        assertEquals(1, descriptions.size());
    }

    @Test
    void testListRights() {
        ResourceConstraint rc = new ResourceConstraint("Open Government Licence","license", "https://eidc.ac.uk/licences/ogl/plain");
        ResourceConstraint rc2 = new ResourceConstraint("Open Government Licence","MPA", "");
        List<Rights> rights = attributes.listRights(List.of(rc,rc2));
        assertEquals("OGL-UK-3.0", rights.get(0).rightsIdentifier());
        assertEquals("", rights.get(1).rightsUri());
    }

    @Test
    void testGatherDistributionFormats() {
        DistributionInfo info = DistributionInfo.builder().name("GeoTIFF").type("Raster").build();
        List<String> formats = attributes.gatherDistributionFormats(List.of(info));
        assertEquals(List.of("Raster GeoTIFF"), formats);
    }

    @Test
    void testCreateRelatedIdentifiers() {
        OnlineResource or = OnlineResource.builder().url("https://data-package.ceh.ac.uk/sd/123").build();
        Supplemental sup = Supplemental.builder().url("https://doi.org/10.1234/example").build();
        Link link = Link.builder().title("https://catalogue.ceh.ac.uk/id/abc").build();
        Link link2 = Link.builder().title("https://catalogue.ceh.ac.uk/id/def").build();
        JenaLookupService jenaService = mock(JenaLookupService.class);

        when(docMock.getUri()).thenReturn("http://localhost:8080/history/rev123/xyz");
        when(docMock.getId()).thenReturn("xyz");

        when(docMock.getInfoLinks()).thenReturn(List.of(or));
        when(docMock.getIncomingCitations()).thenReturn(List.of(sup));
        when(jenaService.relationships(any(), any())).thenReturn(List.of(link));
        when(jenaService.inverseRelationships(any(), any())).thenReturn(List.of(link2));

        List<RelatedIdentifier> result = attributes.createRelatedIdentifiers(docMock, jenaService);
        assertEquals(4, result.size());
    }

    @Test
    void testFilteredOnlineResources() {
        OnlineResource or = OnlineResource.builder().url("https://data-package.ceh.ac.uk/sd/123").build();
        OnlineResource or2 = OnlineResource.builder().url("https://example.com/not-accepted").build();
        List<OnlineResource> filtered = attributes.filteredOnlineResources(List.of(or, or2));
        assertEquals(1, filtered.size());
    }

    @Test
    void testGetAlternateResourceIdentifiers() {

        ResourceIdentifier r1 = ResourceIdentifier.builder().codeSpace("doi").code("YP").build();
        ResourceIdentifier r2 = ResourceIdentifier.builder().codeSpace("https://example.org/other1").code("YP").build();
        ResourceIdentifier r3 = ResourceIdentifier.builder().codeSpace("YP-MPA").code("yum").build();
        ResourceIdentifier r4 = ResourceIdentifier.builder().codeSpace("https://main-resource.com").code("").build();

        List<ResourceIdentifier> input = List.of(r1, r2, r3, r4);

        // When
        List<Identifier> result = attributes.getAlternateResourceIdentifiers(input, "https://main-resource.com");
        // Then
        assertEquals(2, result.size());

        assertEquals("https://example.org/other/1YP", result.get(0).identifier());
        assertEquals("URL", result.get(0).identifierType());

        assertEquals("YP-MPA/yum", result.get(1).identifier());
        assertEquals("URN", result.get(1).identifierType());
    }
    @Test
    void testSetDateDetails() {
        DatasetReferenceDate drd = DatasetReferenceDate.builder()
            .creationDate(LocalDate.of(2023, 1, 1))
            .publicationDate(LocalDate.of(2022, 6, 1)).build();
        List<Date> dates = attributes.setDateDetails(drd);
        assertEquals(2, dates.size());
        assertEquals("2022-06-01", dates.getFirst().date());
    }

    @Test
    void testExtractSubjects() {
        Keyword k1 = Keyword.builder().value("climate").URI("https://www.wikidata.org/entity/Q123").build();
        Keyword k2 = Keyword.builder().value("").URI("https://inspire.ec.europa.eu/theme/abc").build();
        Keyword k3 = Keyword.builder().value("mountains").URI("").build();
        List<Subject> subjects = attributes.extractSubjects(List.of(k1, k2, k3));
        assertEquals(2, subjects.size());
    }

    @Test
    void testPublicationDateCheck() {
        int year = attributes.publicationDateCheck(LocalDate.of(2022, 5, 1));
        assertEquals(2022, year);
        assertEquals(0, attributes.publicationDateCheck(null));
    }

    @Test
    void testAssignPublisher() {
        ResponsibleParty rp = ResponsibleParty.builder()
            .displayName("NERC")
            .organisationIdentifier("https://ror.org/016476m91")
            .familyName("family").build();
        Publisher publisher = attributes.assignPublisher(List.of(rp));
        assertEquals("ROR", publisher.publisherIdentifierScheme());
        assertNull(attributes.assignPublisher(List.of()));
    }

    @Test
    void testDataciteContactCreatorContributor() {
        ResponsibleParty author = ResponsibleParty.builder()
            .displayName("Rick Astley")
            .honorificPrefix(null)
            .familyName(null).build();
        when(docMock.getAuthors()).thenReturn(List.of(author));

        List<DataciteContact> creators = attributes.dataciteContact(docMock, "creator");
        assertEquals(1, creators.size());
        assertEquals("Personal", creators.getFirst().nameType());

        ResponsibleParty contact = ResponsibleParty.builder()
            .displayName("Patrick Stewart")
            .honorificPrefix(null)
            .familyName(null).build();
        when(docMock.getPointsOfContact()).thenReturn(List.of(contact));
        when(docMock.getRightsHolders()).thenReturn(List.of(contact));
        when(docMock.getCustodians()).thenReturn(List.of(contact));
        List<DataciteContact> contributors = attributes.dataciteContact(docMock, "contributor");
        assertEquals(3, contributors.size());
    }

    @Test
    void testDataciteContactHelperNonOrcROR() {
        ResponsibleParty party = ResponsibleParty.builder()
            .displayName("Patrick Stewart")
            .honorificPrefix(null)
            .familyName(null)
            .nameIdentifier("Non-ORC")
            .organisationName("Starfleet")
            .organisationIdentifier("https://ror.org/016476m91").build();
        DataciteContact contact = attributes.dataciteContactHelper(party, "contributor", "ContactPerson");
        assertEquals("Patrick Stewart", contact.name());
        assertEquals("ROR", contact.affiliation().getFirst().affiliationIdentifierScheme());
    }

    @Test
    void testDataciteContactHelperOrganisation() {
        ResponsibleParty party = ResponsibleParty.builder()
            .organisationName("Starfleet")
            .organisationIdentifier("https://ror.org/016476m91")
            .honorificPrefix(null)
            .familyName(null).nameIdentifier("Non-ORC").build();
        DataciteContact contact = attributes.dataciteContactHelper(party, "contributor", "ContactPerson");
        assertEquals("Starfleet", contact.name());
        assertEquals("Organizational", contact.nameType());
        assertEquals("ROR", contact.affiliation().getFirst().affiliationIdentifierScheme());
    }
}
