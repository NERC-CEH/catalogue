package uk.ac.ceh.gateway.catalogue.model;


import org.junit.jupiter.api.Test;
import org.springframework.util.SerializationUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogueUserTest {

    @Test
    void checkCatalogueUserSerializable() {
        //given
        CatalogueUser user = new CatalogueUser("test", "test");

        //when
        byte[] serialize = SerializationUtils.serialize(user);
        CatalogueUser userDeserialized = (CatalogueUser) SerializationUtils.deserialize(serialize);
        assertThat(user, equalTo(userDeserialized));
    }

    @Test
    void checkUserWithNullUsernameIsPublic() {
        //Given
        CatalogueUser user = new CatalogueUser();

        //When
        boolean isPublic = user.isPublic();

        //Then
        assertTrue(isPublic);
    }

    @Test
    void checkUserWithUsernameIsNotPublic() {
        //Given
        CatalogueUser user = new CatalogueUser("james", "james@bo.jones");

        //When
        boolean isPublic = user.isPublic();

        //Then
        assertFalse(isPublic);
    }
}
