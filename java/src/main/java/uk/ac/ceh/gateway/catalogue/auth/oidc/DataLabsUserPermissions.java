package uk.ac.ceh.gateway.catalogue.auth.oidc;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@AllArgsConstructor
public class DataLabsUserPermissions {

    private final Data data;

    public List<String> getUserPermissions(){
        return data.getUserPermissions();
    }

    @Value
    private class Data{
        private List<String> userPermissions;
    }
}
