package uk.ac.ceh.gateway.catalogue.search;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class HardcodedFacetFactoryTest {

    @Test
    void newInstanceCatalogue() {
        val factory = new HardcodedFacetFactory();
        val expected = Facet.builder()
            .fieldName("catalogue")
            .displayName("Catalogue")
            .hierarchical(false)
            .build();

        val facet = factory.newInstance("catalogue");

        assertThat(facet, equalTo(expected));
    }

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
