package uk.ac.ceh.gateway.catalogue.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.ukeof.model.UsernamePassword;

/**
 *
 * @author cjohn
 */
public class AuthenticationControllerTest {
    @Mock(answer=Answers.RETURNS_DEEP_STUBS) AuthenticationManager manager;
    @Mock RememberMeServices rememberme;
    @Mock LogoutHandler logoutHandler;
    private AuthenticationController controller;
    
    @Before
    public void createAuthenticationController() {
        MockitoAnnotations.initMocks(this);
        controller = new AuthenticationController(manager,
                                                  rememberme,
                                                  logoutHandler);
    }
    
    @Test
    public void checkThatCanObtaineMeAsAUser() {
        //Given
        CatalogueUser user = new CatalogueUser();
        
        //When
        CatalogueUser me = controller.me(user);
        
        //Then
        assertEquals("Expected me to be returned by controller", user, me);
    }
    
    @Test
    public void checkLogoutCallsSecurityHandler() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        //When
        controller.logout(request, response);
        
        //Then
        verify(logoutHandler).logout(eq(request), eq(response), any(Authentication.class));
    }
    
    @Test
    public void checkLogoutReturnsALogoutMessage() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        //When
        String message = controller.logout(request, response);
        
        //Then
        assertEquals("Expected a nice logout message", "logged out", message);
    }
    
    @Test
    public void checkUsernameAndPasswordSubmittedToManager() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        UsernamePassword usernamePassword = mock(UsernamePassword.class);
        when(usernamePassword.getUsername()).thenReturn("username");
        when(usernamePassword.getPassword()).thenReturn("password");
        
        //When
        controller.login(request, response, usernamePassword);
        
        //Then
        ArgumentCaptor<UsernamePasswordAuthenticationToken> token = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(manager).authenticate(token.capture());
        
        assertEquals("Expected the same username", "username", token.getValue().getPrincipal());
        assertEquals("Expected the same password", "password", token.getValue().getCredentials());
    }
    
    @Test
    public void checkRememberMeNotifiedOfLoginSuccess() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        UsernamePassword usernamePassword = mock(UsernamePassword.class);
        when(usernamePassword.getUsername()).thenReturn("username");
        when(usernamePassword.getPassword()).thenReturn("password");
        
        Authentication authentication = mock(Authentication.class);
        when(manager.authenticate(any(Authentication.class))).thenReturn(authentication);
        
        //When
        controller.login(request, response, usernamePassword);
        
        //Then
        verify(rememberme).loginSuccess(request, response, authentication);
    }
}
