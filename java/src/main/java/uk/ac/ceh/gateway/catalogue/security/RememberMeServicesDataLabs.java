package uk.ac.ceh.gateway.catalogue.security;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UserStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

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

        // The plus one is because the the substring will include the index of "."
        String userName = token.substring(token.indexOf(".") + 1, token.lastIndexOf("."));

        CatalogueUser catalogueUser = userStore.getUser(userName);
        List<Group> groups = groupStore.getGroups(catalogueUser);

        List<GrantedAuthority> grantedAuthorities = groups.stream().map(group ->
                (GrantedAuthority)() -> group.getName()).collect(Collectors.toList());

        return new RememberMeAuthenticationToken("key", catalogueUser, grantedAuthorities);
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {

    }

}
