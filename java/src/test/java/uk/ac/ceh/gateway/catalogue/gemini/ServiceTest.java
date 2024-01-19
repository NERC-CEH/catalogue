package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ServiceTest {
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Test
    public void readServiceFromString() throws IOException {
        //Given
        String json = "{\"type\":\"view\",\"couplingType\":\"tight\",\"versions\":[\"1.1.1\",\"1.3.0\"],\"coupledResources\":[{\"operationName\":\"GetMap\",\"identifier\":\"123\"},{\"operationName\":\"GetCapabilities\",\"identifier\":\"1234\"}],\"containsOperations\":[{\"operationName\":\"GetMap\",\"platform\":\"WebService\",\"url\":\"url2\"}]}";
        Service expected = Service.builder()
            .type("view")
            .couplingType("tight")
            .versions(Arrays.asList("1.1.1", "1.3.0"))
            .coupledResources(Arrays.asList(
                Service.CoupledResource.builder().operationName("GetMap").identifier("123").build(),
                Service.CoupledResource.builder().operationName("GetCapabilities").identifier("1234").build()
            ))
            .containsOperations(Arrays.asList(
                Service.OperationMetadata.builder().operationName("GetMap")
                    .platform("WebService")
                    .url("url2")
                    .build()
            ))
            .build();

        //When
        Service actual = mapper.readValue(json, Service.class);
        String service = mapper.writeValueAsString(actual);

        //Then
        assertThat("Actual service should equal expected", actual, equalTo(expected));
        assertThat("service should be the same as the input json string", service, equalTo(json));
    }

}
