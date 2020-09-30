package uk.ac.ceh.gateway.catalogue.util;

import lombok.val;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class HeadersTest {

    @Test
    public void withBasicAuth() {
        //given

        //when
        val headers = Headers.withBasicAuth("username", "password");

        //then
        assertTrue(headers.containsKey("Authorization"));
        assertThat(headers.get("Authorization"), contains("Basic dXNlcm5hbWU6cGFzc3dvcmQ="));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        //given

        //when
        val headers = Headers.withBasicAuth(null, null);

        //then
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyStrings() {
        //given

        //when
        val headers = Headers.withBasicAuth("", "");

        //then
        fail();
    }
}
