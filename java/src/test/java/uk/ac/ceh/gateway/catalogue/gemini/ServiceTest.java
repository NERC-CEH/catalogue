package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author rjsc
 */
public class ServiceTest {
    private ObjectMapper mapper;
    
    @Before
    public void setup() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Test
    public void readServiceFromString() throws IOException {
        //Given
        String json = "{\"type\":\"view\",\"couplingType\":\"tight\",\"versions\":[\"1.1.1\",\"1.3.0\"],\"coupledResources\":[{\"operationName\":\"GetMap\",\"identifier\":\"123\"},{\"operationName\":\"GetCapabilities\",\"identifier\":\"1234\"}],\"containsOperations\":[{\"operationName\":\"GetMap\",\"platforms\":[\"WebService\",\"HTTP RPC\"],\"urls\":[\"url2\",\"url3\"]}]}";
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
                    .platforms(Arrays.asList("WebService", "HTTP RPC"))
                    .urls(Arrays.asList("url2", "url3"))
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