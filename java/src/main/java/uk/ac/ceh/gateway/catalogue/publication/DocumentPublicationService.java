package uk.ac.ceh.gateway.catalogue.publication;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.PublicationServiceException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Qualifier("document")
@Slf4j
@ToString
public class DocumentPublicationService implements PublicationService {
    private final GroupStore<CatalogueUser> groupStore;
    private final Workflow workflow;
    private final DocumentRepository documentRepository;
    private final String basePath = "documents";

    public DocumentPublicationService(
        GroupStore<CatalogueUser> groupStore,
        @Qualifier("document") Workflow workflow,
        DocumentRepository documentRepository
    ) {
        this.groupStore = groupStore;
        this.workflow = workflow;
        this.documentRepository = documentRepository;
        log.info("Creating {}", this);
    }

    @Override
    public StateResource current(CatalogueUser user, String fileIdentifier) {
        try {
            MetadataDocument doc = documentRepository.read(fileIdentifier);
            return current(user, doc.getMetadata(), doc.getId());
        } catch (DocumentRepositoryException | NullPointerException ex) {
            throw new PublicationServiceException(String.format("Could not get current state for: %s", fileIdentifier), ex);
        }
    }

    private StateResource current(CatalogueUser user, MetadataInfo metadataInfo, String metadataId) {
        final State currentState = workflow.currentState(metadataInfo);
        return new StateResource(currentState, getPublishingRoles(user, metadataInfo), metadataId, metadataInfo.getCatalogue(), basePath);
    }

    @Override
    public StateResource transition(CatalogueUser user, String fileIdentifier, String transitionId) {
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
            return current(user, doc.getMetadata(), doc.getId());
        } catch (DocumentRepositoryException | NullPointerException ex) {
            throw new PublicationServiceException(String.format("Could not transition: %s", fileIdentifier), ex);
        }
    }

    private Set<PublishingRole> getPublishingRoles(CatalogueUser user, MetadataInfo info) {
        return groupStore.getGroups(user).stream()
            .map(Group::getName)
            .map(String::toLowerCase)
            .filter(name -> name.startsWith(String.format("role_%s", info.getCatalogue())))
            .map(name -> name.substring(name.lastIndexOf("_") + 1))
            .map(name -> new PublishingRole(name))
            .collect(Collectors.toSet());
    }
}
