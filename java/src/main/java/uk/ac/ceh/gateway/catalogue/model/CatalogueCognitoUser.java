package uk.ac.ceh.gateway.catalogue.model;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Profile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Accessors(chain = true)
@Profile("auth:cognito")
public class CatalogueCognitoUser extends CatalogueUser {
    private final List<CatalogueGroup> groups;

    public CatalogueCognitoUser(String username, String email, List<String> groups) {
        super(username, email);
        this.groups = (groups != null)
            ? groups.stream()
                .map(group -> new CatalogueGroup(group.toUpperCase()))
                .collect(Collectors.toList())
            : Collections.emptyList();
    }
}
