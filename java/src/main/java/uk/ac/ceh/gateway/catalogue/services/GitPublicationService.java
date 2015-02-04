package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.publication.PublicationException;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.PublishingRole;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.Transition;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

@Service
public class GitPublicationService implements PublicationService {
    private final GroupStore<CatalogueUser> groupStore;
    private final Workflow workflow;
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;

    @Autowired
    public GitPublicationService(GroupStore<CatalogueUser> groupStore, Workflow workflow, DataRepository<CatalogueUser> repo, DocumentInfoMapper<MetadataInfo> documentInfoMapper) {
        this.groupStore = groupStore;
        this.workflow = workflow;
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
    }

    @Override
    public StateResource current(CatalogueUser user, String fileIdentifier, UriComponentsBuilder builder) {
        return current(user, getMetadataInfo(fileIdentifier), builder);
    }
    
    private StateResource current(CatalogueUser user, MetadataInfo metadataInfo, UriComponentsBuilder builder) {
        final State currentState = workflow.currentState(metadataInfo);
        final List<Group> userGroups = groupStore.getGroups(user);
        return new StateResource(currentState, getPublishingRoles(userGroups), builder);
    }

    @Override
    public StateResource transition(CatalogueUser user, String fileIdentifier, String transitionId, UriComponentsBuilder builder) {
        final MetadataInfo original = getMetadataInfo(fileIdentifier);
        final Set<PublishingRole> publishingRoles = getPublishingRoles(groupStore.getGroups(user));
        final State currentState = workflow.currentState(original);
        final Transition transition = currentState.getTransition(publishingRoles, transitionId);
        final MetadataInfo returned = workflow.transitionDocumentState(original, publishingRoles, transition);
        if ( !returned.equals(original)) {
            saveMetadataInfo(fileIdentifier, returned, user);
        }
        return current(user, returned, builder);
    }
    
    private MetadataInfo getMetadataInfo(String fileIdentifier) {
        MetadataInfo metadataInfo;
        try {
            metadataInfo = documentInfoMapper.readInfo(repo.getData(String.format("%s.meta", fileIdentifier)).getInputStream());
        } catch (IOException | NullPointerException ex) {
            throw new DocumentDoesNotExistException(String.format("Document: %s does not exist", fileIdentifier), ex);
        }
        return metadataInfo;
    }
    
    private Set<PublishingRole> getPublishingRoles(List<Group> groups) {
        Set<PublishingRole> publishingRoles = new HashSet<>();
        groups.stream().forEach((Group group) -> {
            String name = group.getName();
            if (name.startsWith("ROLE_")) {
                publishingRoles.add(new PublishingRole(name));
            }
        });
        return publishingRoles;
    }
    
    private void saveMetadataInfo(String fileIdentifier, MetadataInfo info, CatalogueUser user) {
        try {
            String commitMsg = String.format("Publication state of %s changed to %s", fileIdentifier, info.getState());
            repo.submitData(String.format("%s.meta", fileIdentifier), 
                (o)-> documentInfoMapper
                    .writeInfo(info, o))
                    .commit(user, commitMsg);
        } catch (DataRepositoryException ex) {
            throw new PublicationException(String.format("Unable to change publication state to: %s for %s", info.getState(), fileIdentifier), ex);
        }
    }
}