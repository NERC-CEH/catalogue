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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class RememberMeServicesDataLabsTest {

    @Mock
    private HttpServletResponse response;

    private RSASSASigner signer;

    private static final String COOKIE_NAME = "token";

    private static final String USER_NAME = "tester";

    private static final String ISSUER = "https://mjbr.eu.auth0.com/";

    private static final String WRONG_ISSUER = "https://mjbr.eu.fake.com/";

    private static final long ONE_HOUR = 3600000L;

    private RSAKey privateKey;

    private RSAKey publicKey;

    private JWKSet jwkSet;

    private RememberMeServicesDataLabs target;

    @BeforeEach
    @SneakyThrows
    public void init() {

        privateKey = new RSAKeyGenerator(2048).keyID("123").generate();
        publicKey = new RSAKey.Builder(privateKey.toPublicJWK()).build();
        jwkSet = new JWKSet(publicKey);
        val keySource = new ImmutableJWKSet<>(jwkSet);
        signer = new RSASSASigner(privateKey);

        target = new RememberMeServicesDataLabs(COOKIE_NAME, ISSUER, keySource);
    }


    @Test
    public void successfullyAutoLoginTest() throws JOSEException {

        //Given
        String token = this.getAccessToken(ISSUER, new Date(new Date().getTime() - ONE_HOUR),
                new Date(new Date().getTime() + ONE_HOUR));

        val request = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", token))
                .buildRequest(new MockServletContext());

        //When
        val authentication = target.autoLogin(request, response);

        //Then
        assertThat(((CatalogueUser) authentication.getPrincipal()).getUsername(), is(equalTo(USER_NAME)));
        assertThat(((String) authentication.getCredentials()), is(equalTo(token)));
    }

    @Test
    public void autoLoginTestShouldReturnNull() {

        //Given
        MockHttpServletRequest emptyMockHttpServletRequest = new MockHttpServletRequest();

        //When
        val authentication = target.autoLogin(emptyMockHttpServletRequest, response);

        //Then
        assertThat(authentication, is(nullValue()));
    }

    @Test
    public void autoLoginTestExpiredToken() {
        Assertions.assertThrows(BadJWTException.class, () -> {
            //Given
            String token = this.getAccessToken(ISSUER, new Date(new Date().getTime() - 2 * ONE_HOUR),
                    new Date(new Date().getTime() - ONE_HOUR));

            val request = MockMvcRequestBuilders
                    .get(URI.create("https://example.com"))
                    .cookie(new Cookie("token", token))
                    .buildRequest(new MockServletContext());
            //When
            target.autoLogin(request, response);
        });
    }

    @Test
    public void autoLoginTestIssuerDoesNotMatch() throws JOSEException {
        Assertions.assertThrows(BadJWTException.class, () -> {
            //Given
            String token = this.getAccessToken(WRONG_ISSUER, new Date(new Date().getTime() - ONE_HOUR),
                    new Date(new Date().getTime() + ONE_HOUR));

            val request = MockMvcRequestBuilders
                    .get(URI.create("https://example.com"))
                    .cookie(new Cookie("token", token))
                    .buildRequest(new MockServletContext());

            //When
            target.autoLogin(request, response);
        });

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