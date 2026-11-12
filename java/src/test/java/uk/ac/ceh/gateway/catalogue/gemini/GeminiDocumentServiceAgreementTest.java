package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.serviceagreement.File;
import uk.ac.ceh.gateway.catalogue.serviceagreement.FileGroup;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * What an agreed service agreement copies onto its draft metadata record (dri-one #535).
 */
class GeminiDocumentServiceAgreementTest {

    private static final ResourceConstraint OGL = ResourceConstraint.builder()
        .value("Open Government Licence")
        .code("license")
        .uri("https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
        .build();

    private static ServiceAgreement serviceAgreement() {
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setTitle("Title");
        serviceAgreement.setDepositorName("Depositor, D.");
        serviceAgreement.setDepositorContactDetails("depositor@example.com");
        return serviceAgreement;
    }

    private static File file(String name, String format) {
        val file = new File();
        file.setName(name);
        file.setFormat(format);
        return file;
    }

    private static FileGroup fileGroup(String pattern, String format) {
        val fileGroup = new FileGroup();
        fileGroup.setPattern(pattern);
        fileGroup.setFormat(format);
        return fileGroup;
    }

    private static ResponsibleParty person(String name, String email) {
        return ResponsibleParty.builder().displayName(name).email(email).build();
    }

    @Test
    void additionalUseConstraintsFollowTheLicence() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setEndUserLicence(OGL);
        serviceAgreement.setUseConstraints("Do not publish nest locations");
        val gemini = new GeminiDocument();

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getUseConstraints(), contains(
            OGL,
            ResourceConstraint.builder().value("Do not publish nest locations").code("otherRestrictions").build()
        ));
    }

    @Test
    void additionalUseConstraintsCopiedWithoutALicence() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setUseConstraints("Do not publish nest locations");
        val gemini = new GeminiDocument();

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getUseConstraints(), contains(
            ResourceConstraint.builder().value("Do not publish nest locations").code("otherRestrictions").build()
        ));
    }

    @Test
    void blankAdditionalUseConstraintsAddNothing() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setEndUserLicence(OGL);
        serviceAgreement.setUseConstraints("  ");
        val gemini = new GeminiDocument();

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getUseConstraints(), contains(OGL));
    }

    @Test
    void distinctFileFormatsBecomeDistributionFormats() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setFiles(List.of(
            file("site1/2024.csv", "CSV"),
            file("site2/2024.csv", "csv"),
            file("readme", " "),
            file("grid.nc", "NetCDF ")
        ));
        val gemini = new GeminiDocument();

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getDistributionFormats(), contains(
            DistributionInfo.builder().name("CSV").build(),
            DistributionInfo.builder().name("NetCDF").build()
        ));
    }

    @Test
    void groupFormatsAreCopiedWithFileFormats() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setFiles(List.of(file("site1/2024.csv", "CSV")));
        serviceAgreement.setFileGroups(List.of(
            fileGroup("csv", "csv"),
            fileGroup("grid", "NetCDF")
        ));
        val gemini = new GeminiDocument();

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getDistributionFormats(), contains(
            DistributionInfo.builder().name("CSV").build(),
            DistributionInfo.builder().name("NetCDF").build()
        ));
    }

    @Test
    void noFileFormatsLeavesTheRecordsFormats() {
        val existing = List.of(DistributionInfo.builder().name("Shapefile").build());
        val gemini = new GeminiDocument();
        gemini.setDistributionFormats(existing);

        gemini.populateFromServiceAgreement(serviceAgreement());

        assertThat(gemini.getDistributionFormats(), is(existing));
    }

    @Test
    void agreeingTwiceDoesNotDuplicateContacts() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setAuthors(List.of(person("Author, A.", "author@example.com")));
        serviceAgreement.setOwnersOfIpr(List.of(ResponsibleParty.builder().organisationName("Owner Org").build()));
        val gemini = new GeminiDocument();

        gemini.populateFromServiceAgreement(serviceAgreement);
        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getContactPoints(), hasSize(1));
        assertThat(gemini.getAuthors(), hasSize(1));
        assertThat(gemini.getRightsHolders(), hasSize(1));
    }

    @Test
    void contactAnEditorEnrichedIsNotAddedAgain() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setAuthors(List.of(person("Author, A.", "author@example.com")));
        val enriched = person("author, a.", "Author@Example.com")
            .withNameIdentifier("https://orcid.org/0000-0002-1825-0097");
        val gemini = new GeminiDocument();
        gemini.setAuthors(new ArrayList<>(List.of(enriched)));

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getAuthors(), contains(enriched));
    }

    @Test
    void ukcehAuthorsSharingTheEnquiriesEmailStayDistinct() {
        // Every @ceh.ac.uk email is rewritten to enquiries@ceh.ac.uk, so email alone can't identify a person
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setAuthors(List.of(
            person("Smith, A.", "asmith@ceh.ac.uk"),
            person("Jones, B.", "bjones@ceh.ac.uk")
        ));
        val gemini = new GeminiDocument();

        gemini.populateFromServiceAgreement(serviceAgreement);
        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getAuthors(), contains(
            person("Smith, A.", "enquiries@ceh.ac.uk"),
            person("Jones, B.", "enquiries@ceh.ac.uk")
        ));
    }

    @Test
    void sameNameWithDifferentEmailsIsADifferentPerson() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setAuthors(List.of(person("Smith, A.", "a.smith@example.com")));
        val gemini = new GeminiDocument();
        gemini.setAuthors(new ArrayList<>(List.of(person("Smith, A.", "alan.smith@example.org"))));

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getAuthors(), hasSize(2));
    }

    @Test
    void sameOrcidIsTheSamePersonWhateverTheName() {
        val orcid = "https://orcid.org/0000-0002-1825-0097";
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setAuthors(List.of(person("Carberry, J.", "").withNameIdentifier(orcid)));
        val gemini = new GeminiDocument();
        gemini.setAuthors(new ArrayList<>(List.of(person("Carberry, Josiah", "").withNameIdentifier(orcid))));

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getAuthors(), hasSize(1));
    }

    @Test
    void orcidMatchesWhateverFormItIsWrittenIn() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setAuthors(List.of(person("Carberry, J.", "").withNameIdentifier("https://orcid.org/0000-0002-1825-0097")));
        val gemini = new GeminiDocument();
        gemini.setAuthors(new ArrayList<>(List.of(person("Carberry, Josiah", "").withNameIdentifier("http://www.orcid.org/0000-0002-1825-0097"))));

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getAuthors(), hasSize(1));
    }

    @Test
    void namesakesWithDifferentOrcidsAreDifferentPeople() {
        val serviceAgreement = serviceAgreement();
        serviceAgreement.setAuthors(List.of(person("Smith, A.", "").withNameIdentifier("https://orcid.org/0000-0002-1825-0097")));
        val gemini = new GeminiDocument();
        gemini.setAuthors(new ArrayList<>(List.of(person("Smith, A.", "").withNameIdentifier("https://orcid.org/0000-0001-5109-3700"))));

        gemini.populateFromServiceAgreement(serviceAgreement);

        assertThat(gemini.getAuthors(), hasSize(2));
    }
}
