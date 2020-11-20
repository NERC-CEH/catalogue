package uk.ac.ceh.gateway.catalogue.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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

    private static final String userName = "userName";

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
    public void checkThatTryingToGetValueForMissingContentReturnsNull() throws UnknownUserException {

        //Given
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        Mockito.when(userStore.getUser(userName)).thenReturn(catalogueUser);
        Mockito.when(groupStore.getGroups(catalogueUser)).thenReturn(groups);
        Mockito.when(group.getName()).thenReturn(groupName);

        //When
        RememberMeAuthenticationToken authentication = (RememberMeAuthenticationToken) target.autoLogin(mockHttpServletRequest, response);

        //Then
        assertThat(authentication.getKeyHash(), is(equalTo(keyHash)));
    }
}