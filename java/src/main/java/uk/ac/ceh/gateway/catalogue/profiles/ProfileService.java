package uk.ac.ceh.gateway.catalogue.profiles;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProfileService {
    private final Environment env;

    public ProfileService(Environment env) {
        this.env = env;
        log.info("Creating");
        log.debug(env.toString());
    }

    public boolean isActive(String profiles) {
        val isActive = env.acceptsProfiles(Profiles.of(profiles));
        log.debug("{} is active? {}", profiles, isActive);
        return isActive;
    }
}
