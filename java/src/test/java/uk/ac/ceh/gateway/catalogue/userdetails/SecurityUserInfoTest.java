package uk.ac.ceh.gateway.catalogue.userdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SecurityTestExecutionListeners
class SecurityUserInfoTest {

    @Test
    @WithAnonymousUser
    void isNotLoggedIn() {
        SecurityUserInfo securityUserInfo = new SecurityUserInfo();
        assertFalse(securityUserInfo.isLoggedIn());
    }

    @Test
    @WithMockCatalogueUser
    void isLoggedIn() {
        SecurityUserInfo securityUserInfo = new SecurityUserInfo();
        assertTrue(securityUserInfo.isLoggedIn());
    }

}
