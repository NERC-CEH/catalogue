package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WithMockCatalogueUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCatalogueUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCatalogueUser catalogueUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        val principal = new CatalogueUser(catalogueUser.username(), catalogueUser.email());

        val grantedAuthorities = Arrays.stream(catalogueUser.grantedAuthorities())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication auth = new PreAuthenticatedAuthenticationToken(
                principal,
                "access-token",
                grantedAuthorities
        );
        context.setAuthentication(auth);
        return context;
    }
}
