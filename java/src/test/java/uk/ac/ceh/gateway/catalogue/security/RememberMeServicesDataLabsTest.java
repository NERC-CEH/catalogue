package uk.ac.ceh.gateway.catalogue.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class RememberMeServicesDataLabsTest {

    @Mock
    private UserStore<CatalogueUser> userStore;

    @Mock
    private GroupStore<CatalogueUser> groupStore;

    @Mock
    Group group;

    @Mock
    HttpServletResponse response;

    @Mock
    MockHttpServletRequest mockHttpServletRequest;

    private static final String cookieName = "token";

    private static final String userName = "username";

    private static final String emailAddress = "email";

    private static final String groupName = "groupName";

    private static final int keyHash = 106079;

    private RememberMeServicesDataLabs target;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        target = new RememberMeServicesDataLabs(userStore, groupStore, cookieName);
        mockHttpServletRequest = new MockHttpServletRequest();
        Cookie[] cookies = new Cookie[]{
                new Cookie("token", "value1.username.value3")
        };
        mockHttpServletRequest.setCookies(cookies);
    }

    @Test
    public void autoLoginTest() throws UnknownUserException {

        //Given
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setEmail(emailAddress);
        catalogueUser.setUsername(userName);
        given(userStore.getUser(userName)).willReturn(catalogueUser);
        given(groupStore.getGroups(catalogueUser)).willReturn(groups);

        //When
        RememberMeAuthenticationToken authentication = (RememberMeAuthenticationToken) target.autoLogin(mockHttpServletRequest, response);

        //Then
        assertThat(authentication.getKeyHash(), is(equalTo(keyHash)));
        assertThat(authentication.getPrincipal(), is(equalTo(catalogueUser)));
    }

    @Test
    public void autoLoginTest_EmptyToken() throws UnknownUserException {

        //Given
        MockHttpServletRequest emptyMockHttpServletRequest = new MockHttpServletRequest();
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setEmail(emailAddress);
        catalogueUser.setUsername(userName);

        //When
        RememberMeAuthenticationToken authentication = (RememberMeAuthenticationToken)
                target.autoLogin(emptyMockHttpServletRequest, response);

        //Then
        assertThat(authentication, is(nullValue()));
    }
}