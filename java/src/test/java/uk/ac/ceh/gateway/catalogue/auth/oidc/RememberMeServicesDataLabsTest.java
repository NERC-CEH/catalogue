package uk.ac.ceh.gateway.catalogue.auth.oidc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    private final String TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlFVWTBORFZFTmtaRE1VSkdRek" +
            "V5TXpaRFEwVTBSa0kwTmpRd01rVkZSRUV6TmpoR1FVUkZRdyJ9.eyJpc3MiOiJodHRwczovL21qYnIuZXUuYXV0aDAu" +
            "Y29tLyIsInN1YiI6ImF1dGgwfDVhMGVhZTk3YTM5MmE5NDA3NzViNDBkMSIsImF1ZCI6WyJodHRwczovL2RhdGFsYWIu" +
            "ZGF0YWxhYnMuY2VoLmFjLnVrL2FwaSIsImh0dHBzOi8vbWpici5ldS5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0Ijox" +
            "NjA2MTQ3MDYxLCJleHAiOjE2MDYyMzM0NjEsImF6cCI6Ink1eW56M2E0R1g0dU5pT0pNWGlPZ1hkZk9uMWtGNnNyIiwi" +
            "c2NvcGUiOiJvcGVuaWQgcHJvZmlsZSJ9.bJswm0-Np-IxFVf_uzWnWhvWF-wPlN_5z6MXrI3XkMaHjefPfExrJP66709" +
            "7uXEyVsxgdl-_YNw16euWQQ9FRBnDswShxEHOckZBaGKS30WMRhGvoDKqdFrK_3yU3DBYqfQhC9hdHNj28CHprteP7dU" +
            "NCNczPomkxfoLlXjahEwS3nm3fz583MihxX23MPv3KTyg6XQDc_sXKPkR_cuQlzQMijhPmVy4DbEbzxLkOLR3tva0r5S" +
            "gJwKKpoHseHPqsBpL_uYObiUEfErOyJ4a-qmCMzl-7HD_iKsW5SAPltTYiFW7YnYgJ4EsmEoeWn8_Y6I4zLUGrjQPxL0" +
            "_DBCNfA";

    private static final String CREDENTIALS = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlFVWTBORFZFTmtaRE1VSkdRekV5TXpaRFEwVTBSa0kwTmpRd01rVkZSRUV6TmpoR1FVUkZRdyJ9.eyJpc3MiOiJodHRwczovL21qYnIuZXUuYXV0aDAuY29tLyIsInN1YiI6ImF1dGgwfDVhMGVhZTk3YTM5MmE5NDA3NzViNDBkMSIsImF1ZCI6WyJodHRwczovL2RhdGFsYWIuZGF0YWxhYnMuY2VoLmFjLnVrL2FwaSIsImh0dHBzOi8vbWpici5ldS5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNjA2MTQ3MDYxLCJleHAiOjE2MDYyMzM0NjEsImF6cCI6Ink1eW56M2E0R1g0dU5pT0pNWGlPZ1hkZk9uMWtGNnNyIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSJ9.bJswm0-Np-IxFVf_uzWnWhvWF-wPlN_5z6MXrI3XkMaHjefPfExrJP667097uXEyVsxgdl-_YNw16euWQQ9FRBnDswShxEHOckZBaGKS30WMRhGvoDKqdFrK_3yU3DBYqfQhC9hdHNj28CHprteP7dUNCNczPomkxfoLlXjahEwS3nm3fz583MihxX23MPv3KTyg6XQDc_sXKPkR_cuQlzQMijhPmVy4DbEbzxLkOLR3tva0r5SgJwKKpoHseHPqsBpL_uYObiUEfErOyJ4a-qmCMzl-7HD_iKsW5SAPltTYiFW7YnYgJ4EsmEoeWn8_Y6I4zLUGrjQPxL0_DBCNfA";

    private static final String PRINCIPAL = "auth0|5a0eae97a392a940775b40d1";

    private static final String COOKIE_NAME = "token";

    private static final String USER_NAME = "auth0|5a0eae97a392a940775b40d1";

    private static final String EMAIL_ADDRESS = "email";

    private RememberMeServicesDataLabs target;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        target = new RememberMeServicesDataLabs(userStore, groupStore, COOKIE_NAME);
        mockHttpServletRequest = new MockHttpServletRequest();
        Cookie[] cookies = new Cookie[]{
                new Cookie("token", TOKEN)
        };
        mockHttpServletRequest.setCookies(cookies);
    }

    @Test
    public void autoLoginTest() {

        //Given
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setEmail(EMAIL_ADDRESS);
        catalogueUser.setUsername(USER_NAME);

        //When
        PreAuthenticatedAuthenticationToken authentication = (PreAuthenticatedAuthenticationToken) target.autoLogin(mockHttpServletRequest, response);

        //Then
        assertThat(authentication.getPrincipal().toString(), is(equalTo(PRINCIPAL)));
        assertThat(authentication.getCredentials().toString(), is(equalTo(CREDENTIALS)));
    }

    @Test
    public void autoLoginTest_EmptyToken() {

        //Given
        MockHttpServletRequest emptyMockHttpServletRequest = new MockHttpServletRequest();
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setEmail(EMAIL_ADDRESS);
        catalogueUser.setUsername(USER_NAME);

        //When
        RememberMeAuthenticationToken authentication = (RememberMeAuthenticationToken)
                target.autoLogin(emptyMockHttpServletRequest, response);

        //Then
        assertThat(authentication, is(nullValue()));
    }
}