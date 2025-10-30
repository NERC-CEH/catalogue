package uk.ac.ceh.gateway.catalogue.auth.cognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CognitoGroupStoreTest {

    private CognitoGroupStore groupStore;
    private CatalogueUser user;
    private List<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        groupStore = new CognitoGroupStore();
        user = new CatalogueUser("test-user", "test@test.com");
        authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
    }

    @Test
    void shouldReturnUserGroupsFromSecurityContext() {
        Authentication cognitoAuth = new CognitoAuthenticationToken(
            user,
            authorities,
            null
        );
        SecurityContextHolder.getContext().setAuthentication(cognitoAuth);

        // when
        var groups = groupStore.getGroups(user);

        // then
        assertEquals(2, groups.size());
        assertTrue(groups.stream().anyMatch(g -> g.getName().equals("ROLE_USER")));
        assertTrue(groups.stream().anyMatch(g -> g.getName().equals("ROLE_ADMIN")));
    }

    @Test
    void shouldReturnEmptyListWhenNoAuthorities() {
        // given
        Authentication cognitoAuth = new CognitoAuthenticationToken(
            user,
            new ArrayList<>(),
            null
        );
        SecurityContextHolder.getContext().setAuthentication(cognitoAuth);

        // when
        var groups = groupStore.getGroups(user);

        // then
        assertTrue(groups.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNotAuthenticated() {
        // given
        SecurityContextHolder.clearContext();

        // when
        var groups = groupStore.getGroups(user);

        // then
        assertTrue(groups.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenAuthenticationIsNull() {
        // given
        SecurityContextHolder.getContext().setAuthentication(null);

        // when
        var groups = groupStore.getGroups(user);

        // then
        assertTrue(groups.isEmpty());
    }
}
