package uk.ac.ceh.gateway.catalogue.auth.cognito;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueCognitoUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.userdetails.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Profile("auth:cognito")
@Service
public class CognitoGroupStore implements GroupStore<CatalogueUser> {

    @Override
    public List<Group> getGroups(CatalogueUser user) {
        return new ArrayList<>(((CatalogueCognitoUser) user).getGroups());
    }

    @Override
    public Group getGroup(String name) throws IllegalArgumentException {
        throw new NotImplementedException(format("Cannot get group %s", name));
    }

    @Override
    public List<Group> getAllGroups() {
        throw new NotImplementedException("cannot get all groups");
    }

    @Override
    public boolean isGroupInExistance(String name) {
        throw new NotImplementedException(format("Cannot check for existence of group %s", name));
    }

    @Override
    public boolean isGroupDeletable(String group) throws IllegalArgumentException {
        throw new NotImplementedException(format("Cannot check if deletable group %s", group));
    }
}
