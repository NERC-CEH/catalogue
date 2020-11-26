package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
@Profile("auth:datalabs")
@Service
@ToString
public class RememberMeServicesDataLabs implements RememberMeServices {

    final private UserStore<CatalogueUser> userStore;
    final private GroupStore<CatalogueUser> groupStore;
    final private String cookieName;

    RememberMeServicesDataLabs(UserStore<CatalogueUser> userStore,
                               GroupStore<CatalogueUser> groupStore,
                               String cookieName){
        this.userStore = userStore;
        this.groupStore = groupStore;
        this.cookieName = cookieName;
        log.info("Creating {}", this);
    }

    @SneakyThrows
    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);

        if (cookie == null) {
            return null;
        }

        String token = cookie.getValue();

        val jwsObject = JWSObject.parse(token);
        val payload = jwsObject.getPayload().toJSONObject();
        val claimsSet = JWTClaimsSet.parse(payload);

        log.info(claimsSet.getSubject());
        log.info(claimsSet.getExpirationTime().toString());

        String userName = claimsSet.getSubject();

        return new PreAuthenticatedAuthenticationToken(userName, token);
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {

    }

    private boolean verifyToken(String token, String userName) throws ParseException, JOSEException, BadJOSEException, MalformedURLException {

        // https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
        // Create a JWT processor for the access tokens
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor =
                new DefaultJWTProcessor<>();

        // Set the required "typ" header "at+jwt" for access tokens issued by the
        // Connect2id server, may not be set by other servers
//      The default is 'jwt' so do not need to set this
//        jwtProcessor.setJWSTypeVerifier(
//                new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("at+jwt")));

        // The public RSA keys to validate the signatures will be sourced from the
        // OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
        // object caches the retrieved keys to speed up subsequent look-ups and can
        // also handle key-rollover
        val remoteJwks = "https://mjbr.eu.auth0.com/.well-known/jwks.json";
        JWKSource<SecurityContext> keySource =
                new RemoteJWKSet<>(new URL(remoteJwks));

        // The expected JWS algorithm of the access tokens (agreed out-of-band)
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

        // Configure the JWT processor with a key selector to feed matching public
        // RSA keys sourced from the JWK set URL
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);

        jwtProcessor.setJWSKeySelector(keySelector);

        // Set the required JWT claims for access tokens issued by the Connect2id
        // server, may differ with other servers
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier(
                new JWTClaimsSet.Builder().issuer("https://mjbr.eu.auth0.com/").build(),
                new HashSet<>(Arrays.asList("sub", "aud", "iat", "exp", "scope"))));

        // Process the token
        SecurityContext ctx = null; // optional context parameter, not required here
        JWTClaimsSet claimsSet = jwtProcessor.process(token, ctx);

        // Print out the token claims set
        log.info(claimsSet.toJSONObject().toString());

        log.info(claimsSet.getIssuer());
        log.info(claimsSet.getSubject());
        log.info(claimsSet.getAudience().toString());
        log.info(claimsSet.getExpirationTime().toString());

        if(claimsSet.getSubject() == userName){
            return true;
        }
        return false;
    }

}
