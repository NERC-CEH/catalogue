package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.Test;
import org.springframework.util.SerializationUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CatalogueGroupTest {

    @Test
    void checkCatalogueGroupSerializable() {
        //given
        CatalogueGroup group = new CatalogueGroup("test");

        //when
        byte[] serialize = SerializationUtils.serialize(group);
        CatalogueGroup groupDeserialized = (CatalogueGroup) SerializationUtils.deserialize(serialize);
        assertThat(group, equalTo(groupDeserialized));
    }

}
