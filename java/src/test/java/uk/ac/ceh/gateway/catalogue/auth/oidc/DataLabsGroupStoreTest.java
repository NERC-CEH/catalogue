package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SecurityTestExecutionListeners
public class DataLabsGroupStoreTest {

    private final static String ANY_STRING = "any";

    private final static String ROLE_1 = "ROLE1";

    private final static String ROLE_2 = "ROLE2";

    private GroupStore<CatalogueUser> target;


    @Before
    public void init() {
        target = new DataLabsGroupStore<>();
    }

    @Test
    @WithMockCatalogueUser(grantedAuthorities = {ROLE_1, ROLE_2})
    public void successfullyGetGroups() {

        //Given
        val catalogueUser = new CatalogueUser();
        catalogueUser.setUsername("test");

        //When
        List<Group> groups = target.getGroups(catalogueUser);

        //Then
        assertTrue(groups.get(0).getName().contains(ROLE_1));
        assertTrue(groups.get(1).getName().contains(ROLE_2));
    }

    @Test
    @WithMockCatalogueUser
    public void getGroupsWithEmptyGrantedAuthorities() {
        //Given
        val catalogueUser = new CatalogueUser();
        catalogueUser.setUsername("test");

        //When
        List<Group> groups = target.getGroups(catalogueUser);

        //Then
        assertTrue(groups.isEmpty());
    }

    @Test(expected = Exception.class)
    @WithMockCatalogueUser
    public void userDoesNotMatchPrincipal() {
        //Given
        val catalogueUser = new CatalogueUser();
        catalogueUser.setUsername("not test");

        //When
        target.getGroups(catalogueUser);

        //Then
        fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAllGroupsTest() {
        //When
        target.getAllGroups();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isGroupInExistenceTest() {
        //When
        target.isGroupInExistance(ANY_STRING);
    }
}