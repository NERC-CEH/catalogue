package uk.ac.ceh.gateway.catalogue.publication;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.PublicationServiceException;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementException;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementService;

import java.util.Set;
import java.util.stream.Collectors;
@Profile("service-agreement")
@Service
@Qualifier("service-agreement")
@Slf4j
@ToString
public class ServiceAgreementPublicationService implements PublicationService {
    private final GroupStore<CatalogueUser> groupStore;
    private final Workflow workflow;
    private final String basePath = "service-agreement";
    private final ServiceAgreementService serviceAgreementService;

    public ServiceAgreementPublicationService(
        GroupStore<CatalogueUser> groupStore,
        @Qualifier("service-agreement") Workflow workflow,
        ServiceAgreementService serviceAgreementService
    ) {
        this.groupStore = groupStore;
        this.workflow = workflow;
        this.serviceAgreementService = serviceAgreementService;
        log.info("Creating {}", this);
    }

    @Override
    public StateResource current(CatalogueUser user, String fileIdentifier) {
        try {
            ServiceAgreement doc = serviceAgreementService.get(user, fileIdentifier);
            return current(user, doc);
        } catch (ServiceAgreementException | NullPointerException ex) {
            throw new PublicationServiceException(String.format("Could not get current state for: %s", fileIdentifier), ex);
        }
    }

    public StateResource current(CatalogueUser user, ServiceAgreement doc) {
        try {
            val metadataInfo = doc.getMetadata();
            val metadataId = doc.getId();
            val currentState = workflow.currentState(metadataInfo);
            return new StateResource(currentState, getPublishingRoles(user, doc), metadataId, metadataInfo.getCatalogue(), basePath);
        } catch (ServiceAgreementException | NullPointerException ex) {
            throw new PublicationServiceException(String.format("Could not get current state for: %s", doc.getId()), ex);
        }
    }

    @Override
    public StateResource transition(CatalogueUser user, String fileIdentifier, String transitionId) {
        try {
            final ServiceAgreement doc = serviceAgreementService.get(user, fileIdentifier);
            final MetadataInfo original = doc.getMetadata();
            final Set<PublishingRole> publishingRoles = getPublishingRoles(user, doc);
            final Transition transition = workflow
                .currentState(original)
                .getTransition(publishingRoles, transitionId);
            doc.setMetadata(workflow.transitionDocumentState(original, publishingRoles, transition));
            serviceAgreementService.updateMetadata(
                user,
                fileIdentifier,
                doc.getMetadata()
            );
            serviceAgreementService.doTransitionAction(user, fileIdentifier, transition.getId());
            return current(user, doc);
        } catch (ServiceAgreementException | NullPointerException ex) {
            throw new PublicationServiceException(String.format("Could not transition: %s", fileIdentifier), ex);
        }
    }

    private Set<PublishingRole> getPublishingRoles(CatalogueUser user, ServiceAgreement doc) {
        Set<PublishingRole> publishingRoles = groupStore.getGroups(user).stream()
            .map(Group::getName)
            .map(String::toLowerCase)
            .filter(name -> name.startsWith(String.format("role_%s", doc.getMetadata().getCatalogue())))
            .map(name -> name.substring(name.lastIndexOf("_") + 1))
            .map(PublishingRole::new)
            .collect(Collectors.toSet());

        if (userIsDepositor(user, doc)) {
            publishingRoles.add(new PublishingRole("depositor"));
            publishingRoles.remove(new PublishingRole("publisher"));
        }

        return publishingRoles;
    }

    private boolean userIsDepositor(CatalogueUser user, ServiceAgreement doc) {
        String contactDetails = doc.getDepositorContactDetails().toLowerCase();
        String username = user.getUsername().toLowerCase();
        return doc.getMetadata().getIdentities(Permission.VIEW).contains(username)
               && Set.of(username, username + "@ceh.ac.uk").contains(contactDetails);
    }
}
