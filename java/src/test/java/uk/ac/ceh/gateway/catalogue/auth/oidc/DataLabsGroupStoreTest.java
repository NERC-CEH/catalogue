package uk.ac.ceh.gateway.catalogue.auth.oidc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.User;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DataLabsGroupStoreTest {

    private static String ANY_STRING = "any";

    private static String ROLE1_LOWERCASE = "role1";

    private static String ROLE2_LOWERCASE = "role2";

    private static String ROLE1_UPPERCASE = "ROLE1";

    private static String ROLE2_UPPERCASE = "ROLE2";

    @Mock
    User user;

    @Mock
    SecurityContext securityContext;

    @Mock
    PreAuthenticatedAuthenticationToken authentication;

    private DataLabsGroupStore target;


    @Before
    public void init() {
        target = new DataLabsGroupStore();
    }

    @Test
    public void GetGroupsTest() {

        //Given
        CatalogueUser catalogueUser = new CatalogueUser();
        SecurityContextHolder.setContext(securityContext);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(ROLE1_LOWERCASE);
        GrantedAuthority grantedAuthority2 = new SimpleGrantedAuthority(ROLE2_LOWERCASE);
        grantedAuthorities.add(grantedAuthority);
        grantedAuthorities.add(grantedAuthority2);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getAuthorities()).willReturn(grantedAuthorities);

        //When
        List<Group> groups = target.getGroups(catalogueUser);

        //Then
        assertThat(groups.get(0).getName(), is(equalTo(ROLE1_UPPERCASE)));
        assertThat(groups.get(1).getName(), is(equalTo(ROLE2_UPPERCASE)));
    }

    @Test
    public void GetGroupsTest_NULL() {

        //Given
        CatalogueUser catalogueUser = new CatalogueUser();
        SecurityContextHolder.setContext(securityContext);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getAuthorities()).willReturn(grantedAuthorities);

        //When
        List<Group> groups = target.getGroups(catalogueUser);

        //Then
        assertThat(groups.isEmpty(), is(equalTo(true)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void GetAllGroupsTest() {
        //When
        target.getAllGroups();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void IsGroupInExistanceTest() {
        //When
        target.isGroupInExistance(ANY_STRING);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void IsGroupDeletableTest() {
        //When
        target.isGroupDeletable(ANY_STRING);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void CreateGroupTest() {
        //When
        target.createGroup(ANY_STRING, ANY_STRING);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void UpdateGroupTest() {
        //When
        target.updateGroup(ANY_STRING, ANY_STRING);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void DeleteGroupTest() {
        //When
        target.deleteGroup(ANY_STRING);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void GrantGroupToUser() {
        //When
        target.grantGroupToUser(user, ANY_STRING);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void RevokeGroupFromUser() {
        //When
        target.grantGroupToUser(user, ANY_STRING);
    }
}