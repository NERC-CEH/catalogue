package uk.ac.ceh.gateway.catalogue.search;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class HardcodedFacetFactoryTest {

    @Test
    void newInstance() {
        //given
        val factory = new HardcodedFacetFactory();
        val expected =  Facet.builder()
            .fieldName("resourceType")
            .displayName("Resource type")
            .hierarchical(false)
            .build();

        //when
        val facet = factory.newInstance("resourceType");

        //then
        assertThat(facet, equalTo(expected));
    }
}
