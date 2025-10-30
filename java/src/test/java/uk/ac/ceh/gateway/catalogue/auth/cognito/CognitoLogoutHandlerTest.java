package uk.ac.ceh.gateway.catalogue.auth.cognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CognitoLogoutHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private CognitoLogoutHandler logoutHandler;

    @BeforeEach
    void setUp() {
        logoutHandler = new CognitoLogoutHandler();

        // Set private fields using reflection
        setPrivateField(logoutHandler, "userPoolDomain", "https://test.auth.us-east-1.amazoncognito.com");
        setPrivateField(logoutHandler, "userPoolClientId", "test-client-id");
        setPrivateField(logoutHandler, "logoutRedirectUrl", "http://localhost:8080");
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    @Test
    void shouldBuildCorrectLogoutUrl() {
        // when
        String result = logoutHandler.determineTargetUrl(request, response, authentication);

        // then
        String expected = "https://test.auth.us-east-1.amazoncognito.com/logout" +
            "?client_id=test-client-id" +
            "&logout_uri=http://localhost:8080";
        assertEquals(expected, result);
    }

    @Test
    void shouldHandleEmptyProperties() {
        // given
        setPrivateField(logoutHandler, "userPoolDomain", "");
        setPrivateField(logoutHandler, "userPoolClientId", "");
        setPrivateField(logoutHandler, "logoutRedirectUrl", "");

        // when
        String result = logoutHandler.determineTargetUrl(request, response, authentication);

        // then
        String expected = "/logout?client_id=&logout_uri=";
        assertEquals(expected, result);
    }
}
