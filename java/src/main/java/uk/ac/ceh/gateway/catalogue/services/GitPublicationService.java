package uk.ac.ceh.gateway.catalogue.services;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.PublishingRole;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.Transition;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;

@Service
public class GitPublicationService implements PublicationService {
    private final GroupStore<CatalogueUser> groupStore;
    private final Workflow workflow;
    private final MetadataInfoEditingService metadataInfoEditingService;

    @Autowired
    public GitPublicationService(GroupStore<CatalogueUser> groupStore, Workflow workflow, MetadataInfoEditingService metadataInfoEditingService) {
        this.groupStore = groupStore;
        this.workflow = workflow;
        this.metadataInfoEditingService = metadataInfoEditingService;
    }

    @Override
    public StateResource current(CatalogueUser user, String fileIdentifier, UriComponentsBuilder builder, URI metadataUrl) {
        MetadataDocument doc = metadataInfoEditingService.getMetadataDocument(fileIdentifier, metadataUrl);
        return current(user, doc.getMetadata(), builder, doc.getTitle(), doc.getUri());
    }
    
    private StateResource current(CatalogueUser user, MetadataInfo metadataInfo, UriComponentsBuilder builder, String metadataTitle, URI metadataUrl) {
        final State currentState = workflow.currentState(metadataInfo);
        final List<Group> userGroups = groupStore.getGroups(user);
        return new StateResource(currentState, getPublishingRoles(userGroups), builder, metadataTitle, metadataUrl.toString());
    }

    @Override
    public StateResource transition(CatalogueUser user, String fileIdentifier, String transitionId, UriComponentsBuilder builder, URI metadataUrl) {
        final MetadataDocument doc = metadataInfoEditingService.getMetadataDocument(fileIdentifier, metadataUrl);
        final MetadataInfo original = doc.getMetadata();
        final Set<PublishingRole> publishingRoles = getPublishingRoles(groupStore.getGroups(user));
        final State currentState = workflow.currentState(original);
        final Transition transition = currentState.getTransition(publishingRoles, transitionId);
        final MetadataInfo returned = workflow.transitionDocumentState(original, publishingRoles, transition);
        if ( !returned.equals(original)) {
            String commitMsg = String.format("Publication state of %s changed to %s", fileIdentifier, returned.getState());
            metadataInfoEditingService.saveMetadataInfo(fileIdentifier, returned, user, commitMsg);
        }
        return current(user, returned, builder, doc.getTitle(), doc.getUri());
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
}