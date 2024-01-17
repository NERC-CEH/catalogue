package uk.ac.ceh.gateway.catalogue.userdetails;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.UnknownUserException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueGroup;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class CrowdGroupStoreTest {
    private GroupStore<CatalogueUser> groupStore;
    private MockRestServiceServer mockServer;

    private final CatalogueUser user = new CatalogueUser()
        .setUsername("foo")
        .setEmail("foo@example.com");

    @BeforeEach
    void setup() {
        val restTemplate = new RestTemplate();
        groupStore = new CrowdGroupStore(
                "https://example.com/latest",
                "abc",
                "1234",
                restTemplate
                );
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getGroup() {
        //given
        val response = """
        {
            "groups": [
            { "name": "group-1" },
            { "name": "group-2" },
            { "name": "group-3" }
            ]
        }
        """;
        mockServer.expect(requestTo("https://example.com/latest/user/group/nested?username=foo"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("authorization", "Basic YWJjOjEyMzQ="))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        val groups = groupStore.getGroups(user);

        //then
        assertThat(groups, contains(
                    new CatalogueGroup("group-1"),
                    new CatalogueGroup("group-2"),
                    new CatalogueGroup("group-3")
                    ));
    }

    @Test
    void groupNotFound() {
        //given
        mockServer.expect(requestTo("https://example.com/latest/user/group/nested?username=foo"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        //when
        assertThrows(UnknownUserException.class, () ->
                groupStore.getGroups(user)
                );
    }

    @Test
    void groupNotImplemented() {
        //when
        assertThrows(NotImplementedException.class, () ->
                groupStore.getGroup("test")
                );
    }

    @Test
    void allGroupsNotImplemented() {
        //when
        assertThrows(NotImplementedException.class, () ->
                groupStore.getAllGroups()
                );
    }

    @Test
    void isGroupInExistenceNotImplemented() {
        //when
        assertThrows(NotImplementedException.class, () ->
                groupStore.isGroupInExistance("test")
                );
    }

    @Test
    void isGroupDeletableNotImplemented() {
        //when
        assertThrows(NotImplementedException.class, () ->
                groupStore.isGroupDeletable("test")
                );
    }
}
