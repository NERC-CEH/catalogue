package uk.ac.ceh.gateway.catalogue.util;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HeadersTest {

    @Test
    public void withBasicAuth() {
        //given

        //when
        val headers = Headers.withBasicAuth("username", "password");

        //then
        assertTrue(headers.containsKey("Authorization"));
        assertThat(headers.get("Authorization").toString(), is("[Basic dXNlcm5hbWU6cGFzc3dvcmQ=]"));
    }

    @Test
    public void testNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //given

            //when
            val headers = Headers.withBasicAuth(null, null);

            //then
            fail();
        });
    }

    @Test
    public void testEmptyStrings() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //given

            //when
            val headers = Headers.withBasicAuth("", "");

            //then
            fail();
        });
    }
}
