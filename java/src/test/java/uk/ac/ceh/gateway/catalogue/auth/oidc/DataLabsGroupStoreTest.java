package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DataLabsGroupStoreTest {

    private static String ANY_STRING = "any";

    private static String ROLE1_LOWERCASE = "role1";

    private static String ROLE2_LOWERCASE = "role2";

    private static String ROLE1_UPPERCASE = "ROLE1";

    private static String ROLE2_UPPERCASE = "ROLE2";

    private GroupStore<CatalogueUser> target;


    @Before
    public void init() {
        target = new DataLabsGroupStore<>();
    }

    @Test
    public void successfullyGetGroups() {

        //Given
        val securityContext = SecurityContextHolder.createEmptyContext();
        val catalogueUser = new CatalogueUser();
        catalogueUser.setUsername("username");
        val authentication = getAuthentication(catalogueUser);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        //When
        List<Group> groups = target.getGroups(catalogueUser);

        //Then
        assertThat(groups.get(0).getName().contains(ROLE1_UPPERCASE), is(equalTo(true)));
        assertThat(groups.get(1).getName().contains(ROLE2_UPPERCASE), is(equalTo(true)));
    }

    @Test
    public void getGroupsWithEmptyGrantedAuthorities() {

        //Given
        val securityContext = SecurityContextHolder.createEmptyContext();
        val catalogueUser = new CatalogueUser();
        val authentication = getEmptyAuthentication(catalogueUser);

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        //When
        List<Group> groups = target.getGroups(catalogueUser);

        //Then
        assertThat(groups.isEmpty(), is(equalTo(true)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAllGroupsTest() {
        //When
        target.getAllGroups();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isGroupInExistanceTest() {
        //When
        target.isGroupInExistance(ANY_STRING);
    }

    private PreAuthenticatedAuthenticationToken getAuthentication(CatalogueUser catalogueUser){
        val grantedAuthorities = Arrays.asList(
                new SimpleGrantedAuthority(ROLE1_LOWERCASE),
                new SimpleGrantedAuthority(ROLE2_LOWERCASE)
        );

    return new PreAuthenticatedAuthenticationToken(catalogueUser, null, grantedAuthorities);
    }

    private PreAuthenticatedAuthenticationToken getEmptyAuthentication(CatalogueUser catalogueUser){
        Collection<? extends GrantedAuthority> grantedAuthorities = Arrays.asList();

        return new PreAuthenticatedAuthenticationToken(catalogueUser, null, grantedAuthorities);
    }
}