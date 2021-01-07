package uk.ac.ceh.gateway.catalogue.auth.oidc;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCatalogueUserSecurityContextFactory.class)
public @interface WithMockCatalogueUser {
    String username() default "test";
    String email() default "test@example.com";
    String[] grantedAuthorities() default {};
}
