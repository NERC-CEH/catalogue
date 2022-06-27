package uk.ac.ceh.gateway.catalogue.gemini;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResourceIdentifierTest {

    @Test
    public void getDoiCoupleResource() {
        //Given
        ResourceIdentifier identifier = ResourceIdentifier.builder()
                                        .code("10.5285/05e5d538-6be7-476d-9141-76d9328738a4")
                                        .codeSpace("doi:")
                                        .build();
        String expected = "doi:10.5285/05e5d538-6be7-476d-9141-76d9328738a4";
        
        //When
        String actual = identifier.getCoupledResource();
        
        //Then
        assertThat("actual coupled resource string should equal expected", actual, equalTo(expected));
        assertThat("other identifier is not an internal identifier", identifier.isInternal(), equalTo(false));
    }
}