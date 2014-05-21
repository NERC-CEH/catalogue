package uk.ac.ceh.gateway.catalogue.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.User;
import uk.ac.ceh.ukeof.model.UsernamePassword;

/**
 * The following controller handles the login and logout. 
 * @author cjohn
 */
public class AuthenticationController {
    private final AuthenticationManager manager;
    private final RememberMeServices rememberme;
    private final LogoutHandler logoutHandler;
    
    @Autowired
    public AuthenticationController(
            @Qualifier("authenticationManager") AuthenticationManager manager,
            RememberMeServices rememberme,
            LogoutHandler logoutHandler) {
        this.manager = manager;
        this.rememberme = rememberme;
        this.logoutHandler = logoutHandler;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public User me(@ActiveUser User user) {
        return user;
    }
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object login(HttpServletRequest request,
                        HttpServletResponse response,
                        @Valid @RequestBody UsernamePassword usernamePassword) {
        Authentication authenticate = manager.authenticate(new UsernamePasswordAuthenticationToken(
                                                                usernamePassword.getUsername(), 
                                                                usernamePassword.getPassword()));
        rememberme.loginSuccess(request, response, authenticate);
        return authenticate.getPrincipal();
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        logoutHandler.logout(request, response, 
            SecurityContextHolder.getContext().getAuthentication());
        return "logged out";
    }
}
