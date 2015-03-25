package uk.ac.ceh.gateway.catalogue.services;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.crowd.model.CrowdGroup;
import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.EDITOR_ROLE;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;
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
        return new StateResource(currentState, getPublishingRoles(user, metadataInfo), builder, metadataTitle, metadataUrl.toString());
    }

    @Override
    public StateResource transition(CatalogueUser user, String fileIdentifier, String transitionId, UriComponentsBuilder builder, URI metadataUrl) {
        final MetadataDocument doc = metadataInfoEditingService.getMetadataDocument(fileIdentifier, metadataUrl);
        final MetadataInfo original = doc.getMetadata();
        final Set<PublishingRole> publishingRoles = getPublishingRoles(user, original);
        final State currentState = workflow.currentState(original);
        final Transition transition = currentState.getTransition(publishingRoles, transitionId);
        final MetadataInfo returned = workflow.transitionDocumentState(original, publishingRoles, transition);
        if ( !returned.equals(original)) {
            String commitMsg = String.format("Publication state of %s changed to %s", fileIdentifier, returned.getState());
            metadataInfoEditingService.saveMetadataInfo(fileIdentifier, returned, user, commitMsg);
        }
        return current(user, returned, builder, doc.getTitle(), doc.getUri());
    }
    
    private Set<PublishingRole> getPublishingRoles(CatalogueUser user, MetadataInfo info) {
        final List<Group> groups = groupStore.getGroups(user);
        if (info.canAccess(Permission.EDIT, user, groups)) {
            groups.add(new CrowdGroup(EDITOR_ROLE));
        }
        return groups.stream()
            .map(Group::getName)
            .filter(name -> name.startsWith("ROLE_"))
            .map(name -> new PublishingRole(name))
            .collect(Collectors.toSet());
    }
}