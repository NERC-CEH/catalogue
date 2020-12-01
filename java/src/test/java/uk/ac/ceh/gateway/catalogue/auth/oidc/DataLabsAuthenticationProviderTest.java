package uk.ac.ceh.gateway.catalogue.auth.oidc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DataLabsAuthenticationProviderTest {

    private static final String CREDENTIALS = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlFVWTBORFZFTmtaRE1VSkdRekV5TXpaRFEwVTBSa0kwTmpRd01rVkZSRUV6TmpoR1FVUkZRdyJ9.eyJpc3MiOiJodHRwczovL21qYnIuZXUuYXV0aDAuY29tLyIsInN1YiI6ImF1dGgwfDVhMGVhZTk3YTM5MmE5NDA3NzViNDBkMSIsImF1ZCI6WyJodHRwczovL2RhdGFsYWIuZGF0YWxhYnMuY2VoLmFjLnVrL2FwaSIsImh0dHBzOi8vbWpici5ldS5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNjA2MTQ3MDYxLCJleHAiOjE2MDYyMzM0NjEsImF6cCI6Ink1eW56M2E0R1g0dU5pT0pNWGlPZ1hkZk9uMWtGNnNyIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSJ9.bJswm0-Np-IxFVf_uzWnWhvWF-wPlN_5z6MXrI3XkMaHjefPfExrJP667097uXEyVsxgdl-_YNw16euWQQ9FRBnDswShxEHOckZBaGKS30WMRhGvoDKqdFrK_3yU3DBYqfQhC9hdHNj28CHprteP7dUNCNczPomkxfoLlXjahEwS3nm3fz583MihxX23MPv3KTyg6XQDc_sXKPkR_cuQlzQMijhPmVy4DbEbzxLkOLR3tva0r5SgJwKKpoHseHPqsBpL_uYObiUEfErOyJ4a-qmCMzl-7HD_iKsW5SAPltTYiFW7YnYgJ4EsmEoeWn8_Y6I4zLUGrjQPxL0_DBCNfA";

    private static final String PRINCIPAL = "auth0|5a0eae97a392a940775b40d1";

    private static final String ADDRESS = "address";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DataLabsUserPermissions dataLabsUserPermissions;

    @Mock
    private ResponseEntity<DataLabsUserPermissions> responseEntity;

    private DataLabsAuthenticationProvider target;

    @Before
    public void init() {
      target = new DataLabsAuthenticationProvider(restTemplate, ADDRESS);
    }

    @Test
    public void authenticateTest() {

        //Given
        PreAuthenticatedAuthenticationToken input = new PreAuthenticatedAuthenticationToken(PRINCIPAL, CREDENTIALS);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer" + CREDENTIALS);
        HttpEntity<String> request = new HttpEntity<>(headers);

        given(restTemplate.exchange(this.ADDRESS, HttpMethod.GET,
                request, DataLabsUserPermissions.class)).willReturn(responseEntity);

        given(responseEntity.getBody()).willReturn(dataLabsUserPermissions);

        //When
        PreAuthenticatedAuthenticationToken authentication = (PreAuthenticatedAuthenticationToken) target.authenticate(input);

        //Then
        assertThat(authentication.getPrincipal().toString(), is(equalTo(PRINCIPAL)));
        assertThat(authentication.getCredentials().toString(), is(equalTo(dataLabsUserPermissions.toString())));
    }

    @Test
    public void authenticateTest_EmptyInput() {

        //When
        PreAuthenticatedAuthenticationToken authentication = (PreAuthenticatedAuthenticationToken) target.authenticate(null);

        //Then
        assertThat(authentication, is(equalTo(null)));
    }

    @Test
    public void supportsTest() {

        //When
        Boolean result = target.supports(PreAuthenticatedAuthenticationToken.class);

        //Then
        assertThat(result, is(equalTo(true)));
    }

    @Test
    public void supportsTest_False() {

        //When
        Boolean result = target.supports(Object.class);

        //Then
        assertThat(result, is(equalTo(false)));
    }
}