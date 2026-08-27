package uk.ac.ceh.gateway.catalogue.gemini;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class ResponsiblePartyTest {

    @Test
    public void authorIsHumanReadable() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        String expected = "author";

        //When
        String actual = author.getRoleDisplayName();

        //Then
        assertThat("actual role should equal expected", actual, equalTo(expected));
    }

    @Test
    public void resourceProviderIsHumanReadable() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("resourceProvider").build();
        String expected = "resource provider";

        //When
        String actual = author.getRoleDisplayName();

        //Then
        assertThat("actual role should equal expected", actual, equalTo(expected));
    }


    @Test
    public void hyphenatedContributorRoleIsHumanReadable() {
        //Given SCoRO contributor roles are hyphenated, not camel case
        ResponsibleParty contributor = ResponsibleParty.builder().contributorRole("data-creator").build();

        //When
        String actual = contributor.getContributorRoleDisplayName();

        //Then
        assertThat("hyphen should render as a word break", actual, equalTo("data creator"));
    }

    @Test
    public void multiWordHyphenatedContributorRoleIsHumanReadable() {
        //Given
        ResponsibleParty contributor = ResponsibleParty.builder().contributorRole("workpackage-leader").build();

        //When
        String actual = contributor.getContributorRoleDisplayName();

        //Then
        assertThat("hyphen should render as a word break", actual, equalTo("workpackage leader"));
    }

    @Test
    public void singleWordContributorRoleIsUnchanged() {
        //Given
        ResponsibleParty contributor = ResponsibleParty.builder().contributorRole("researcher").build();

        //When
        String actual = contributor.getContributorRoleDisplayName();

        //Then
        assertThat(actual, equalTo("researcher"));
    }

    @Test
    public void legacyCamelCaseContributorRoleStillReadable() {
        //Given records saved before the SCoRO rename still hold camel case
        ResponsibleParty contributor = ResponsibleParty.builder().contributorRole("dataCreator").build();

        //When
        String actual = contributor.getContributorRoleDisplayName();

        //Then
        assertThat(actual, equalTo("data creator"));
    }

}
