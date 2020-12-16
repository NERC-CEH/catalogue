package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RememberMeServicesDataLabsTest {

    @Mock
    private Group group;

    @Mock
    private HttpServletResponse response;

    private RSASSASigner signer;

    private MockHttpServletRequest mockHttpServletRequest;

    private static final String COOKIE_NAME = "token";

    private static final String USER_NAME = "tester";

    private static final String ISSUER = "https://mjbr.eu.auth0.com/";

    private static final String WRONG_ISSUER = "https://mjbr.eu.fake.com/";

    private RSAKey privateKey;

    private RSAKey publicKey;

    private JWKSet jwkSet;

    private RememberMeServicesDataLabs target;

    @Before
    @SneakyThrows
    public void init() {

        privateKey = new RSAKeyGenerator(2048).keyID("123").generate();
        publicKey = new RSAKey.Builder(privateKey.toPublicJWK()).build();
        jwkSet = new JWKSet(publicKey);
        val keySource = new ImmutableJWKSet<>(jwkSet);

        this.signer = new RSASSASigner(privateKey);

        target = new RememberMeServicesDataLabs(COOKIE_NAME, ISSUER, keySource);
    }


    @Test
    public void successfullyAutoLoginTest() throws JOSEException {

        //Given
        String token = this.getAccessToken(ISSUER, new Date(new Date().getTime()),
                new Date(new Date().getTime() - 90 * 1000000000));

        Cookie[] cookies = new Cookie[]{
                new Cookie("token", token)
        };
        mockHttpServletRequest = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", token))
                .buildRequest(new MockServletContext());

        mockHttpServletRequest.setCookies(cookies);

        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setUsername(USER_NAME);

        //When
        val authentication = target.autoLogin(mockHttpServletRequest, response);

        //Then
        assertThat(((CatalogueUser)authentication.getPrincipal()).getUsername(), is(equalTo(USER_NAME)));
        assertThat(((String)authentication.getCredentials()), is(equalTo(token)));
    }

    @Test
    public void autoLoginTestShouldReturnNull() throws JOSEException {

        //Given
        String token = this.getAccessToken(ISSUER, new Date(new Date().getTime()),
                new Date(new Date().getTime() - 90 * 1000000000));
        Cookie[] cookies = new Cookie[]{
                new Cookie("token", token)
        };
        mockHttpServletRequest = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", token))
                .buildRequest(new MockServletContext());

        mockHttpServletRequest.setCookies(cookies);

        MockHttpServletRequest emptyMockHttpServletRequest = new MockHttpServletRequest();
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setUsername(USER_NAME);

        //When
        val authentication = target.autoLogin(emptyMockHttpServletRequest, response);

        //Then
        assertThat(authentication, is(nullValue()));
    }

    @Test(expected = BadJWTException.class)
    public void autoLoginTestExpiredToken() throws JOSEException {

        //Given
        String token = this.getAccessToken(ISSUER, new Date(new Date().getTime() - 90 * 1000),
                new Date(new Date().getTime() - 60 * 1000));

        Cookie[] cookies = new Cookie[]{
                new Cookie("token", token)
        };
        mockHttpServletRequest = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", token))
                .buildRequest(new MockServletContext());

        mockHttpServletRequest.setCookies(cookies);

        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setUsername(USER_NAME);

        //When
        target.autoLogin(mockHttpServletRequest, response);
    }

    @Test(expected = BadJWTException.class)
    public void autoLoginTestIssuerDoesNotMatch() throws JOSEException {

        //Given
        String token = this.getAccessToken(WRONG_ISSUER, new Date(new Date().getTime()),
                new Date(new Date().getTime() - 90 * 1000000000));

        Cookie[] cookies = new Cookie[]{
                new Cookie("token", token)
        };
        mockHttpServletRequest = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", token))
                .buildRequest(new MockServletContext());

        mockHttpServletRequest.setCookies(cookies);

        List<Group> groups = new ArrayList<>();
        groups.add(group);
        CatalogueUser catalogueUser = new CatalogueUser();
        catalogueUser.setUsername(USER_NAME);

        //When
        target.autoLogin(mockHttpServletRequest, response);

    }

    private String getAccessToken(String issuer, Date issueTime, Date expirationTime) throws JOSEException {

        val claimsSet = new JWTClaimsSet.Builder()
                .subject("tester")
                .issuer(issuer)
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .audience(Arrays.asList("https://datalab.datalabs.ceh.ac.uk/api", "https://mjbr.eu.auth0.com/userinfo"))
                .claim("scope", "openid profile")
                .build();
        val signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(publicKey.getKeyID()).build(),
                claimsSet);

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}