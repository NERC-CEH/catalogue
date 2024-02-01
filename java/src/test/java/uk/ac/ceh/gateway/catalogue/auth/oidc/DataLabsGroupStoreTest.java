package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SecurityTestExecutionListeners
public class DataLabsGroupStoreTest {

    private final static String ANY_STRING = "any";
    private final static String ROLE_1 = "ROLE1";
    private final static String ROLE_2 = "ROLE2";

    private GroupStore<CatalogueUser> target;

    @BeforeEach
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

    @Test
    @WithMockCatalogueUser
    public void userDoesNotMatchPrincipal() {
        Assertions.assertThrows(Exception.class, () -> {
            //Given
            val catalogueUser = new CatalogueUser();
            catalogueUser.setUsername("not test");

            //When
            target.getGroups(catalogueUser);

            //Then
            fail();
        });
    }

    @Test
    public void getAllGroupsTest() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            //When
            target.getAllGroups();
        });
    }

    @Test
    public void isGroupInExistenceTest() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            //When
            target.isGroupInExistance(ANY_STRING);
        });
    }
}
