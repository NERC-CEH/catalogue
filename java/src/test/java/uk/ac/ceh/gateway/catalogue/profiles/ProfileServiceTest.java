package uk.ac.ceh.gateway.catalogue.profiles;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileServiceTest {

    @Test
    void testProfileIsActive() {
        //given
        val env = new StandardEnvironment();
        env.setActiveProfiles("test");
        val profileService = new ProfileService(env);

        //when
        val isActive = profileService.isActive("test");

        //then
        assertTrue(isActive);
    }

    @Test
    void testProfileIsNotActive() {
        //given
        val env = new StandardEnvironment();
        env.setActiveProfiles("another");
        val profileService = new ProfileService(env);

        //when
        val isActive = profileService.isActive("test");

        //then
        assertFalse(isActive);
    }
}
