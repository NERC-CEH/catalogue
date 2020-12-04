package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class HubbubServiceTest {
    private HubbubService hubbubService;
    private MockRestServiceServer mockServer;
    private byte[] success;

    @Before
    @SneakyThrows
    public void setup() {
        val restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        hubbubService = new HubbubService(
                restTemplate,
                "https://example.com/",
                "hubbub",
                "password01234"
        );
        success = IOUtils.toByteArray(getClass().getResource("hubbub-eidchub-data-true-response.json"));
    }

    @Test
    public void postWithGuid() {
        //given
        mockServer
                .expect(requestTo(startsWith("https://example.com/move_all")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(queryParam("guid", "12345-903"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
                .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        hubbubService.post("/move_all", "12345-903");

        //then
        mockServer.verify();
    }

    @Test
    public void postWithPath() {
        //given
        mockServer
                .expect(requestTo(startsWith("https://example.com/cancel")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(queryParam("path", "12345-903"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
                .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        hubbubService.post("/cancel", "12345-903");

        //then
        mockServer.verify();
    }

    @Test
    public void postQuery() {
        //given
        mockServer
                .expect(requestTo(startsWith("https://example.com/move")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(queryParam("path", "12345-903"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
                .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        hubbubService.postQuery("/move", "12345-903", "to", "supporting-documents");

        //then
        mockServer.verify();
    }

    @Test
    public void delete() {
        //given
        mockServer
                .expect(requestTo(startsWith("https://example.com/delete")))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(queryParam("path", "12345-903"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
                .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        hubbubService.delete("12345-903");

        //then
        mockServer.verify();
    }

    @Test
    public void getJustPath() {
        //given
        mockServer
                .expect(requestTo(startsWith("https://example.com/")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("data", "true"))
                .andExpect(queryParam("path", "12345-903"))
                .andExpect(queryParam("page", "1"))
                .andExpect(queryParam("size", "20"))
                .andExpect(queryParam("status", nullValue()))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
                .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        val actual = hubbubService.get("12345-903");

        //then
        assertThat(actual.getPagination().getTotal(), equalTo(2));
        assertThat(actual.getData().size(), equalTo(2));
        mockServer.verify();
    }

    @Test
    public void getForStatus() {
        //given
        mockServer
                .expect(requestTo(startsWith("https://example.com/")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("data", "true"))
                .andExpect(queryParam("path", "12345-903"))
                .andExpect(queryParam("page", "1"))
                .andExpect(queryParam("size", "20"))
                .andExpect(queryParam("status", "one", "two"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aHViYnViOnBhc3N3b3JkMDEyMzQ="))
                .andRespond(withSuccess(success, MediaType.APPLICATION_JSON));

        //when
        hubbubService.get("12345-903", 1, "one", "two");

        //then
        mockServer.verify();
    }
}
