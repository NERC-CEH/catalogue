package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class DataLabsAuthenticationProviderTest {

    private static final String CREDENTIALS = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlFVWTBORFZFTmtaRE1VSkdRekV5TXpaRFEwVTBSa0kwTmpRd01rVkZSRUV6TmpoR1FVUkZRdyJ9.eyJpc3MiOiJodHRwczovL21qYnIuZXUuYXV0aDAuY29tLyIsInN1YiI6ImF1dGgwfDVhMGVhZTk3YTM5MmE5NDA3NzViNDBkMSIsImF1ZCI6WyJodHRwczovL2RhdGFsYWIuZGF0YWxhYnMuY2VoLmFjLnVrL2FwaSIsImh0dHBzOi8vbWpici5ldS5hdXRoMC5jb20vdXNlcmluZm8iXSwiaWF0IjoxNjA2MTQ3MDYxLCJleHAiOjE2MDYyMzM0NjEsImF6cCI6Ink1eW56M2E0R1g0dU5pT0pNWGlPZ1hkZk9uMWtGNnNyIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSJ9.bJswm0-Np-IxFVf_uzWnWhvWF-wPlN_5z6MXrI3XkMaHjefPfExrJP667097uXEyVsxgdl-_YNw16euWQQ9FRBnDswShxEHOckZBaGKS30WMRhGvoDKqdFrK_3yU3DBYqfQhC9hdHNj28CHprteP7dUNCNczPomkxfoLlXjahEwS3nm3fz583MihxX23MPv3KTyg6XQDc_sXKPkR_cuQlzQMijhPmVy4DbEbzxLkOLR3tva0r5SgJwKKpoHseHPqsBpL_uYObiUEfErOyJ4a-qmCMzl-7HD_iKsW5SAPltTYiFW7YnYgJ4EsmEoeWn8_Y6I4zLUGrjQPxL0_DBCNfA";

    private static final String PRINCIPAL = "auth0|5a0eae97a392a940775b40d1";

    private static final String ADDRESS = "address";

    private DataLabsAuthenticationProvider target;

    private MockRestServiceServer mockServer;

    @Before
    public void init() {
        val restTemplate = new RestTemplate();
        target = new DataLabsAuthenticationProvider(restTemplate, ADDRESS);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @SneakyThrows
    public void successfullyAddGrantedAuthorities() {
        //Given
        val input = new PreAuthenticatedAuthenticationToken(PRINCIPAL, CREDENTIALS);
        val response = IOUtils.toByteArray(getClass().getResource("userPermissions.json"));
        mockServer.expect(requestTo(ADDRESS))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer" + CREDENTIALS))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //When
        Authentication actual = target.authenticate(input);

        //Then
        assertTrue(actual.isAuthenticated());
        assertThat(actual.getAuthorities().toString().contains("CIG_SYSTEM_ADMIN"), is(equalTo(true)));
        assertThat(actual.getAuthorities().toString().contains("ROLE_DATALABS_PUBLISHER"), is(equalTo(true)));
        assertThat(actual.getAuthorities().toString().contains("ROLE_DATALABS_EDITOR"), is(equalTo(true)));
        mockServer.verify();
    }

    @Test
    public void addGrantedAuthoritiesReturnNull() {

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
    public void supportsTestShouldReturnFalse() {

        //When
        Boolean result = target.supports(Object.class);

        //Then
        assertThat(result, is(equalTo(false)));
    }
}