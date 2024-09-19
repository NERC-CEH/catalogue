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
import jakarta.servlet.http.Cookie;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class RememberMeServicesDataLabsTest {
    @Mock
    private CatalogueUserProvider catalogueUserProvider;

    private RSASSASigner signer;

    private static final String COOKIE_NAME = "token";
    private static final String USER_NAME = "tester";
    private static final String EMAIL = "tester@example.com";
    private static final String ISSUER = "https://mjbr.eu.auth0.com/";
    private static final String WRONG_ISSUER = "https://mjbr.eu.fake.com/";
    private static final long ONE_HOUR = 3600000L;

    private RSAKey publicKey;

    private RememberMeServicesDataLabs target;

    private void givenValidCatalogueUser(String token) {
        val user = new CatalogueUser(EMAIL, EMAIL);
        given(catalogueUserProvider.provide(USER_NAME, token))
            .willReturn(user);
    }

    @BeforeEach
    @SneakyThrows
    public void init() {
        val privateKey = new RSAKeyGenerator(2048).keyID("123").generate();
        publicKey = new RSAKey.Builder(privateKey.toPublicJWK()).build();
        val jwkSet = new JWKSet(publicKey);
        val keySource = new ImmutableJWKSet<>(jwkSet);
        signer = new RSASSASigner(privateKey);

        target = new RememberMeServicesDataLabs(COOKIE_NAME, ISSUER, keySource, catalogueUserProvider);
    }

    @Test
    public void successfullyAutoLoginTest() throws JOSEException {
        //Given
        val tokenValid = getAccessToken(
            ISSUER,
            new Date(new Date().getTime() - ONE_HOUR),
            new Date(new Date().getTime() + ONE_HOUR)
        );
        givenValidCatalogueUser(tokenValid);

        val request = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", tokenValid))
                .buildRequest(new MockServletContext());

        //When
        val authentication = target.autoLogin(request, null);

        //Then
        assertThat(((CatalogueUser) authentication.getPrincipal()).getUsername(), is(equalTo(EMAIL)));
        assertThat(((CatalogueUser) authentication.getPrincipal()).getEmail(), is(equalTo(EMAIL)));
        assertThat(((String) authentication.getCredentials()), is(equalTo(tokenValid)));
    }

    @Test
    void autoLoginTestShouldReturnNull() {
        //Given
        val emptyMockHttpServletRequest = new MockHttpServletRequest();

        //When
        val authentication = target.autoLogin(emptyMockHttpServletRequest, null);

        //Then
        assertThat(authentication, is(nullValue()));
    }

    @Test
    void autoLoginTestExpiredToken() throws JOSEException {
        //Given
        val tokenExpired = getAccessToken(
            ISSUER,
            new Date(new Date().getTime() - 2 * ONE_HOUR),
            new Date(new Date().getTime() - ONE_HOUR)
        );

        val request = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", tokenExpired))
                .buildRequest(new MockServletContext());
        //When
        Assertions.assertThrows(BadJWTException.class, () ->
            target.autoLogin(request, null)
        );

        //then
        verifyNoInteractions(catalogueUserProvider);
    }

    @Test
    void autoLoginTestIssuerDoesNotMatch() throws JOSEException {

        //Given
        val tokenWithWrongIssuer = getAccessToken(
            WRONG_ISSUER,
            new Date(new Date().getTime() - ONE_HOUR),
            new Date(new Date().getTime() + ONE_HOUR)
        );

        val request = MockMvcRequestBuilders
                .get(URI.create("https://example.com"))
                .cookie(new Cookie("token", tokenWithWrongIssuer))
                .buildRequest(new MockServletContext());

        //When
        Assertions.assertThrows(BadJWTException.class, () ->
            target.autoLogin(request, null)
        );

        //then
        verifyNoInteractions(catalogueUserProvider);
    }

    private String getAccessToken(String issuer, Date issueTime, Date expirationTime) throws JOSEException {

        val claimsSet = new JWTClaimsSet.Builder()
                .subject(USER_NAME)
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
