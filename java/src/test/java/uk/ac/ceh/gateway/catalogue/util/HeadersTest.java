package uk.ac.ceh.gateway.catalogue.util;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class HeadersTest {

    @Test
    public void withBasicAuth() {
        //given

        //when
        val headers = Headers.withBasicAuth("username", "password");

        //then
        assertTrue(headers.containsHeader("Authorization"));
        assertThat(headers.get("Authorization").toString(), is("[Basic dXNlcm5hbWU6cGFzc3dvcmQ=]"));
    }

    @Test
    public void withBearerTokenStripsSurroundingWhitespace() {
        //given a token with a trailing newline (e.g. read from a Kubernetes secret)

        //when
        val headers = Headers.withBearerToken("my-token\n");

        //then the header value is single-line — no CR/LF the JDK client would reject
        val authorization = headers.getFirst("Authorization");
        assertThat(authorization, is("Bearer my-token"));
        assertTrue(authorization.indexOf('\n') < 0, "header value must not contain a newline");
        assertTrue(authorization.indexOf('\r') < 0, "header value must not contain a carriage return");
    }

    @Test
    public void testNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //given

            //when
            Headers.withBasicAuth(null, null);

            //then
            fail();
        });
    }

    @Test
    public void testEmptyStrings() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //given

            //when
            Headers.withBasicAuth("", "");

            //then
            fail();
        });
    }
}
