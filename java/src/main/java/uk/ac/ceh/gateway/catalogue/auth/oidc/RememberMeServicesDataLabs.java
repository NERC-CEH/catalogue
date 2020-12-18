package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
@Profile("auth:datalabs")
@Service
@ToString
public class RememberMeServicesDataLabs implements RememberMeServices {

    private final String cookieName;
    private final JWTProcessor<SecurityContext> jwtProcessor;


    RememberMeServicesDataLabs(@Value("${datalabs.cookieName}") String cookieName,
                               @Value("${datalabs.issuer}") String issuer,
                               JWKSource<SecurityContext> keySource) {
        this.cookieName = cookieName;
        this.jwtProcessor = createJwtProcessor(keySource, issuer);
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
        val claimsSet = jwtProcessor.process(token, null);
        val user = new CatalogueUser();
        user.setUsername(claimsSet.getSubject());

        return new PreAuthenticatedAuthenticationToken(user, token);
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {

    }

    private ConfigurableJWTProcessor<SecurityContext> createJwtProcessor(JWKSource<SecurityContext> keySource, String issuer) {
        val jwtProcessor = new DefaultJWTProcessor<>();
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(issuer).build(),
                new HashSet<>(Arrays.asList("sub", "aud", "iat", "exp", "scope"))));
        return jwtProcessor;
    }

}
