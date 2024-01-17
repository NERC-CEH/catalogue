package uk.ac.ceh.gateway.catalogue.wms;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("ConstantConditions")
class WmsFormatParameterFilterTest {

    @Test
    @SneakyThrows
    void hidesFormatQueryParameterValue() {
        //given
        val request = new MockHttpServletRequest();
        request.addParameter("format", "image/png");
        val response = new MockHttpServletResponse();
        val filterChain = new MockFilterChain();
        val filter = new WmsFormatParameterFilter();

        //when
        filter.doFilter(request, response, filterChain);
        val actual = filterChain.getRequest().getParameter("format");

        //then
        assertThat(actual, is(nullValue()));
    }

    @Test
    @SneakyThrows
    void doesNotHideQueryParameterValue() {
        //given
        val request = new MockHttpServletRequest();
        request.addParameter("style", "green");
        val response = new MockHttpServletResponse();
        val filterChain = new MockFilterChain();
        val filter = new WmsFormatParameterFilter();

        //when
        filter.doFilter(request, response, filterChain);
        val actual = filterChain.getRequest().getParameter("style");

        //then
        assertThat(actual, equalTo("green"));
    }

}
