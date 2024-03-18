package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class LocalFileGroupStoreTest {

    private final LocalFileGroupStore<CatalogueUser> localFileGroupStore = new LocalFileGroupStore<>(
        "src/test/resources/uk/ac/ceh/gateway/catalogue/auth/oidc/test-roles.json"
    );

    @Test
    void getGroups() {
        //given
        val testUser = new CatalogueUser("test@example.com", "test@example.com");

        //when
        val actual = localFileGroupStore.getGroups(testUser);

        //then
        assertThat(actual.size(), equalTo(2));
        assertThat(actual.get(0).getName(), equalTo("ROLE_EIDC_EDITOR"));
        assertThat(actual.get(1).getName(), equalTo("ROLE_EIDC_PUBLISHER"));
    }

    @Test
    void getGroupsForNull() {
        //given

        //when
        val actual = localFileGroupStore.getGroups(null);

        //then
        assertThat(actual.size(), equalTo(0));
    }

    @Test
    void getGroupsForEmptyUser() {
        //given
        val testUser = CatalogueUser.PUBLIC_USER;

        //when
        val actual = localFileGroupStore.getGroups(testUser);

        //then
        assertThat(actual.size(), equalTo(0));
    }
}
