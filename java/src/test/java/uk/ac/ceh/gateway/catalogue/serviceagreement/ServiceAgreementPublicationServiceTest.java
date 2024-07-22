package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.config.ServiceAgreementPublicationConfig;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.publication.*;
import uk.ac.ceh.gateway.catalogue.publication.ServiceAgreementPublicationService;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ServiceAgreementPublicationServiceTest {
    @Mock GroupStore<CatalogueUser> groupStore;
    @Qualifier("service-agreement")
    Workflow workflow;
    CatalogueUser publisher, depositor;
    private static final String FILENAME = "e5090602-6ff9-4936-8217-857ea6de5774";
    private static final String SUBMITTED_ID = "r18oq";
    private ServiceAgreement draft;
    @Mock
    private ServiceAgreementService serviceAgreementService;
    private ServiceAgreementPublicationService publicationService;

    @BeforeEach
    public void given() throws IOException {
        workflow = new ServiceAgreementPublicationConfig().workflow();
        publisher = new CatalogueUser("Ron MacDonald", "ron@example.com");
        depositor = new CatalogueUser("Ham Burglar", "ham@example.com");

        this.draft = new ServiceAgreement();
        draft.setId(FILENAME);
        draft.setTitle("Test Draft Service Agreement");
        draft.setMetadata(MetadataInfo.builder().state("draft").catalogue("eidc").build());
        draft.setDepositorContactDetails("ham@example.com");

        this.publicationService = new ServiceAgreementPublicationService(groupStore, workflow, serviceAgreementService);
    }

    @Test
    public void successfullyTransitionState() throws Exception {
        //Given
        when(groupStore.getGroups(publisher)).thenReturn(Collections.singletonList(createGroup("ROLE_EIDC_PUBLISHER")));
        when(serviceAgreementService.get(publisher, FILENAME)).thenReturn(draft);

        //When
        publicationService.transition(publisher, FILENAME, SUBMITTED_ID);

        //Then
        verify(serviceAgreementService).get(publisher, FILENAME);
        verify(serviceAgreementService).updateMetadata(publisher, draft.getId(), draft.getMetadata());
    }

    @Test
    public void depositorCanSuccessfullyTransitionState() throws Exception {
        //Given
        MetadataInfo exampleMetadata = MetadataInfo.builder().state("draft").catalogue("eidc").build();
        exampleMetadata.addPermission(Permission.EDIT, publisher.getUsername());

        draft.setMetadata(exampleMetadata);
        when(serviceAgreementService.get(depositor, FILENAME)).thenReturn(draft);

        //When
        publicationService.transition(depositor, FILENAME, SUBMITTED_ID);

        //Then
        verify(serviceAgreementService).get(depositor, FILENAME);
        verify(serviceAgreementService).updateMetadata(depositor, draft.getId(), draft.getMetadata());
    }

    @Test
    public void successfullyGetCurrentState() throws Exception {
        //Given
        when(serviceAgreementService.get(publisher, FILENAME)).thenReturn(draft);

        //When
        StateResource current = publicationService.current(publisher, FILENAME);

        //Then
        verify(serviceAgreementService).get(publisher, FILENAME);
        assertThat("State should be draft", current.getId(), equalTo("draft"));
    }

    @Test
    public void successfullyGetCurrentStateWithoutCallingGetServiceAgreement() throws Exception {
        //Given

        //When
        StateResource current = publicationService.current(publisher, draft);

        //Then
        verify(serviceAgreementService, never()).get(any(CatalogueUser.class), anyString());
        assertThat("Service agreement state should be draft", current.getId(), equalTo("draft"));
    }

    @Test
    public void tryToGetFileThatDoesNotExist() {

        String fileDoesNotExist = "this file name does not exist";
        Assertions.assertThrows(PublicationServiceException.class, () -> {
            //Given
            when(serviceAgreementService.get(publisher, fileDoesNotExist)).thenThrow(new PublicationServiceException("test"));

            //When
            publicationService.current(publisher, fileDoesNotExist);

            //Then
            verify(serviceAgreementService).get(publisher, FILENAME);
        });
    }

    private Group createGroup(String groupname) {
        return new Group() {
            @Override
            public String getName() {
                return groupname;
            }
            @Override
            public String getDescription() {
                return groupname;
            }
        };
    }
}
