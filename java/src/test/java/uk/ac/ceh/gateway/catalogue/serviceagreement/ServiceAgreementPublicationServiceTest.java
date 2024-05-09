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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceAgreementPublicationServiceTest {
    @Mock GroupStore<CatalogueUser> groupStore;
    @Qualifier("service-agreement")
    Workflow workflow;
    CatalogueUser editor;
    private static final String FILENAME = "e5090602-6ff9-4936-8217-857ea6de5774";
    private static final String SUBMITTED_ID = "r18oq";
    private ServiceAgreement draft;
    @Mock
    private ServiceAgreementService serviceAgreementService;
    private ServiceAgreementPublicationService publicationService;

    @BeforeEach
    public void given() throws IOException {
        workflow = new ServiceAgreementPublicationConfig().workflow();
        editor = new CatalogueUser( "Ron MacDonald", "ron@example.com");

        this.draft = new ServiceAgreement();
        draft.setId("e5090602-6ff9-4936-8217-857ea6de5774");
        draft.setTitle("Test Draft Service Agreement");
        draft.setMetadata(MetadataInfo.builder().state("draft").catalogue("eidc").build());

        this.publicationService = new ServiceAgreementPublicationService(groupStore, workflow, serviceAgreementService);
    }

    @Test
    public void successfullyTransitionState() throws Exception {
        //Given
        when(groupStore.getGroups(editor)).thenReturn(Collections.singletonList(createGroup("ROLE_EIDC_EDITOR")));
        when(serviceAgreementService.get(FILENAME)).thenReturn(draft);

        //When
        publicationService.transition(editor, FILENAME, SUBMITTED_ID);

        //Then
        verify(serviceAgreementService).get(FILENAME);
        verify(serviceAgreementService).updateMetadata(editor, draft.getId(), draft.getMetadata());
    }

    @Test
    public void depositorCanSuccessfullyTransitionState() throws Exception {
        //Given
        MetadataInfo exampleMetadata = MetadataInfo.builder().state("draft").catalogue("eidc").build();
        exampleMetadata.addPermission(Permission.EDIT, editor.getUsername());

        draft.setMetadata(exampleMetadata);
        when(serviceAgreementService.get(FILENAME)).thenReturn(draft);

        //When
        publicationService.transition(editor, FILENAME, SUBMITTED_ID);

        //Then
        verify(serviceAgreementService).get(FILENAME);
        verify(serviceAgreementService).updateMetadata(editor, draft.getId(), draft.getMetadata());
    }

    @Test
    public void successfullyGetCurrentState() throws Exception {
        //Given
        when(serviceAgreementService.get(FILENAME)).thenReturn(draft);

        //When
        StateResource current = publicationService.current(editor, FILENAME);

        //Then
        verify(serviceAgreementService).get(FILENAME);
        assertThat("State is should be draft", current.getId(), equalTo("draft"));
    }

    @Test
    public void tryToGetFileThatDoesNotExist() {

        String fileDoesNotExist = "this file name does not exist";
        Assertions.assertThrows(PublicationServiceException.class, () -> {
            //Given
            when(serviceAgreementService.get(fileDoesNotExist)).thenThrow(new PublicationServiceException("test"));

            //When
            publicationService.current(editor, fileDoesNotExist);

            //Then
            verify(serviceAgreementService).get(FILENAME);
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
