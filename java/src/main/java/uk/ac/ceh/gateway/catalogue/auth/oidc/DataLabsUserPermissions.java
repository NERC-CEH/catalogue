package uk.ac.ceh.gateway.catalogue.auth.oidc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@lombok.Data
public class DataLabsUserPermissions {
    Data data;

    public List<String> getUserPermissions() {
        return Optional.ofNullable(data.getUserPermissions())
            .orElse(Collections.emptyList());
    }

    @lombok.Data
    public static class Data {
        List<String> userPermissions;
    }
}
