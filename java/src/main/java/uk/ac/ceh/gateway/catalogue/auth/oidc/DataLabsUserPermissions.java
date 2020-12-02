package uk.ac.ceh.gateway.catalogue.auth.oidc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Value;

import java.util.List;

@Value
@JsonRootName("data")
public class DataLabsUserPermissions {
    List<String> userPermissions;

    @JsonCreator
    public DataLabsUserPermissions(@JsonProperty("userPermissions") List<String> userPermissions) {
        this.userPermissions = userPermissions;
    }
}
