package uk.ac.ceh.gateway.catalogue.services;

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
import uk.ac.ceh.gateway.catalogue.model.PublicationServiceException;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.PublishingRole;
import uk.ac.ceh.gateway.catalogue.publication.State;
import uk.ac.ceh.gateway.catalogue.publication.Transition;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Service
public class GitPublicationService implements PublicationService {
    private final GroupStore<CatalogueUser> groupStore;
    private final Workflow workflow;
    private final DocumentRepository documentRepository;

    @Autowired
    public GitPublicationService(GroupStore<CatalogueUser> groupStore, Workflow workflow, DocumentRepository documentRepository) {
        this.groupStore = groupStore;
        this.workflow = workflow;
        this.documentRepository = documentRepository;
    }

    @Override
    public StateResource current(CatalogueUser user, String fileIdentifier, UriComponentsBuilder builder) {
        try {
            MetadataDocument doc = documentRepository.read(fileIdentifier);
            return current(user, doc.getMetadata(), builder, doc.getId());
        } catch (DocumentRepositoryException | NullPointerException ex) {
            throw new PublicationServiceException(String.format("Could not get current state for: %s", fileIdentifier), ex);
        }
    }
    
    private StateResource current(CatalogueUser user, MetadataInfo metadataInfo, UriComponentsBuilder builder, String metadataId) {
        final State currentState = workflow.currentState(metadataInfo);
        return new StateResource(currentState, getPublishingRoles(user, metadataInfo), builder, metadataId);
    }

    @Override
    public StateResource transition(CatalogueUser user, String fileIdentifier, String transitionId, UriComponentsBuilder builder) {
        try {
            final MetadataDocument doc = documentRepository.read(fileIdentifier);
            final MetadataInfo original = doc.getMetadata();
            final Set<PublishingRole> publishingRoles = getPublishingRoles(user, original);
            final Transition transition = workflow
                .currentState(original)
                .getTransition(publishingRoles, transitionId);
            doc.setMetadata(workflow.transitionDocumentState(original, publishingRoles, transition));
            documentRepository.save(
                user,
                doc,
                fileIdentifier,
                String.format("Publication state of %s changed.", fileIdentifier)
            );
            return current(user, doc.getMetadata(), builder, doc.getId());
        } catch (DocumentRepositoryException | NullPointerException ex) {
            throw new PublicationServiceException(String.format("Could not transition: %s", fileIdentifier), ex);
        }
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