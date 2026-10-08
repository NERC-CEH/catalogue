package uk.ac.ceh.gateway.catalogue.repository.checks;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.InvalidRelationshipTargetException;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.model.RelationshipTargetCheckException;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Each case saves a document as {@code cosmos}. Where it adds a restricted relationship, the
 * check compares against the stored version, so {@code cosmos} is stubbed as absent (a create)
 * unless the case is about what was already stored.
 */
@ExtendWith(MockitoExtension.class)
class RelationshipTargetsTest {
    @Mock
    DocumentIdentifierService documentIdentifierService;
    @Mock
    BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock
    PermissionService permissionService;

    private RelationshipTargets relationshipTargets;

    @BeforeEach
    void setup() {
        relationshipTargets = new RelationshipTargets(documentIdentifierService, documentBundleReader, permissionService);
        // Relationship targets are visible unless a test says otherwise
        lenient().when(permissionService.toAccess(any(), any(), eq("VIEW"))).thenReturn(true);
    }

    private void check(CatalogueUser user, MetadataDocument document) {
        relationshipTargets.check(user, document, "cosmos", StoredVersion.of(documentBundleReader, "cosmos"));
    }

    private static final String UTILISES = "https://digital.ceh.ac.uk/ontology/doo/utilises";
    private static final String BASE_URI = "https://catalogue.ceh.ac.uk";

    private GeminiDocument datasetUtilising(String... targets) {
        GeminiDocument document = (GeminiDocument) new GeminiDocument()
            .setMetadata(MetadataInfo.builder().build());
        document.setRelationships(Arrays.stream(targets)
            .map(target -> new Relationship(UTILISES, target))
            .collect(Collectors.toSet()));
        return document;
    }

    @Test
    @SneakyThrows
    void addingAUtilisesLinkToAMonitoringFacilitySaves() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising(BASE_URI + "/id/morley");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("morley")).willReturn(new MonitoringFacility().setType("monitoringFacility"));

        //When / Then
        assertDoesNotThrow(() -> check(user, document));
    }

    @Test
    @SneakyThrows
    void addingAUtilisesLinkByBareIdSaves() {
        // The editor's record picker stores the target as the bare record id (Solr's identifier
        // field), not a URI; Jena indexing resolves it against the base URI. Rejecting bare ids
        // made every "Produced at" link created in the editor a 400.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("640ccee2-17c9-4b98-8de1-5dc3f6848c63");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("640ccee2-17c9-4b98-8de1-5dc3f6848c63")).willReturn(new MonitoringFacility().setType("monitoringFacility"));

        //When / Then
        assertDoesNotThrow(() -> check(user, document));
    }

    @Test
    @SneakyThrows
    void addingAUtilisesLinkByBareIdToAnotherKindOfRecordThrows() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("another-dataset");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("another-dataset")).willReturn(new GeminiDocument().setType("dataset"));

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void addingAUtilisesLinkToAMonitoringNetworkSaves() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising(BASE_URI + "/id/cosmos-uk");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("cosmos-uk")).willReturn(new MonitoringNetwork().setType("monitoringNetwork"));

        //When / Then
        assertDoesNotThrow(() -> check(user, document));
    }

    @Test
    @SneakyThrows
    void addingAUtilisesLinkToAnotherKindOfRecordThrows() {
        // The editor only offers facilities and networks, but the API takes whatever JSON it is given.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising(BASE_URI + "/id/another-dataset");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("another-dataset")).willReturn(new GeminiDocument().setType("dataset"));

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void addingAUtilisesLinkOutsideTheCatalogueThrows() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("https://example.com/id/morley");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void aUtilisesTargetThatIsNotADocumentIdIsNeverRead() {
        // The id comes from user-supplied JSON, so it must not be able to steer the datastore read.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising(BASE_URI + "/id/../config");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
        verify(documentBundleReader, never()).readBundle("../config");
    }

    @Test
    @SneakyThrows
    void aUtilisesTargetThatCannotBeReadThrows() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising(BASE_URI + "/id/deleted");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("deleted")).willThrow(new GitFileNotFoundException("no such file"));

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void aStoredInvalidUtilisesLinkDoesNotBlockSaving() {
        // A bad link that predates the check must not make the record uneditable; its page and
        // JSON drop the link instead. Only links a save introduces are checked.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        String target = BASE_URI + "/id/another-dataset";
        given(documentBundleReader.readBundle("cosmos")).willReturn(datasetUtilising(target));
        GeminiDocument incoming = datasetUtilising(target);
        incoming.setTitle("A corrected title");

        //When / Then
        assertDoesNotThrow(() -> check(user, incoming));
        verify(documentBundleReader, never()).readBundle("another-dataset");
    }


    private GeminiDocument geminiRelating(String type, String relation, String target) {
        GeminiDocument document = (GeminiDocument) new GeminiDocument()
            .setType(type)
            .setMetadata(MetadataInfo.builder().build());
        document.setRelationships(Set.of(new Relationship(relation, target)));
        return document;
    }

    @Test
    @SneakyThrows
    void replacingARecordOfTheSameTypeSaves() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = geminiRelating("dataset", "http://purl.org/dc/terms/replaces", "old-version");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("old-version")).willReturn(new GeminiDocument().setType("dataset"));

        //When / Then
        assertDoesNotThrow(() -> check(user, document));
    }

    @Test
    @SneakyThrows
    void replacingARecordOfAnotherTypeThrows() {
        // dri-one #439: every restricted relationship is checked, not only doo:utilises
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = geminiRelating("dataset", "http://purl.org/dc/terms/replaces", "a-service");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("a-service")).willReturn(new GeminiDocument().setType("service"));

        //When / Then
        InvalidRelationshipTargetException thrown = assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
        assertThat(thrown.getMessage())
            .contains("a-service")
            .contains("which links to dataset");
    }

    @Test
    @SneakyThrows
    void requiringSomethingOtherThanADataResourceThrows() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = geminiRelating("service", "http://purl.org/dc/terms/requires", "morley");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("morley")).willReturn(new MonitoringFacility().setType("monitoringFacility"));

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void anOpenRelationshipIsNeverChecked() {
        // "Related" deliberately takes any record, so its target is not even read.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = geminiRelating("dataset", "http://purl.org/dc/terms/relation", "https://example.com/anything");

        //When / Then
        assertDoesNotThrow(() -> check(user, document));
        verify(documentBundleReader, never()).readBundle("https://example.com/anything");
    }

    @Test
    @SneakyThrows
    void aTargetTheUserCannotViewIsRejectedWithoutBeingRead() {
        // Otherwise accepting or rejecting the save would reveal whether a draft or private record
        // exists, and roughly what type it is. A facility the user cannot see is refused exactly as
        // a missing record is, and its content is never read.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("private-facility");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(permissionService.toAccess(user, "private-facility", "VIEW")).willReturn(false);

        //When
        InvalidRelationshipTargetException cannotView = assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );

        //Then
        verify(documentBundleReader, never()).readBundle("private-facility");
        assertThat(cannotView.getMessage())
            .isEqualTo(missingTargetMessage(user, "private-facility"));
    }

    @Test
    @SneakyThrows
    void aTargetThatDoesNotExistIsRejectedLikeOneTheUserCannotView() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("no-such-record");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(permissionService.toAccess(user, "no-such-record", "VIEW"))
            .willThrow(new PermissionDeniedException(
                "No document found for: no-such-record", new GitFileNotFoundException("no such file")));

        //When / Then
        InvalidRelationshipTargetException missing = assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
        assertThat(missing.getMessage())
            .isEqualTo(missingTargetMessage(user, "no-such-record"));
    }

    /** The message for an unusable target: the same whether it is missing, hidden or the wrong type. */
    @SneakyThrows
    private String missingTargetMessage(CatalogueUser user, String target) {
        given(permissionService.toAccess(user, "wrong-type", "VIEW")).willReturn(true);
        given(documentBundleReader.readBundle("wrong-type")).willReturn(new GeminiDocument().setType("dataset"));
        GeminiDocument wrongType = datasetUtilising("wrong-type");
        String message = assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, wrongType)
        ).getMessage();
        return message.replace("wrong-type", target);
    }

    @Test
    @SneakyThrows
    void aDatastoreFaultCheckingViewPermissionIsNotReportedAsABadTarget() {
        // CrowdPermissionService wraps every read fault - not only a missing record - in
        // PermissionDeniedException. A fault says nothing about the target, so it must not come
        // back as "the wrong kind of record"; the user should simply try again.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("morley");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(permissionService.toAccess(user, "morley", "VIEW")).willThrow(new PermissionDeniedException(
            "No document found for: morley", new IOException("SMB read timed out")));

        //When / Then
        assertThrows(
            RelationshipTargetCheckException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void aDatastoreFaultReadingTheTargetIsNotReportedAsABadTarget() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("morley");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("morley")).willThrow(new IOException("pack file corrupt"));

        //When / Then
        assertThrows(
            RelationshipTargetCheckException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void aLinkDocumentIsNeverATarget() {
        // Link documents are being retired: a relationship belongs on the record one stands in
        // for. Even one naming a facility is rejected, and the record it names is never read, so
        // a link document to a hidden record cannot reveal anything about it.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = datasetUtilising("link-to-morley");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        // Its own type is normally empty, but the API will store one: even a link document claiming
        // to be a facility is rejected
        given(documentBundleReader.readBundle("link-to-morley")).willReturn(LinkDocument.builder()
            .linkedDocumentId("morley").build().setType("monitoringFacility"));

        //When
        InvalidRelationshipTargetException thrown = assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );

        //Then
        verify(documentBundleReader, never()).readBundle("morley");
        verify(permissionService, never()).toAccess(user, "morley", "VIEW");
        assertThat(thrown.getMessage())
            .isEqualTo(missingTargetMessage(user, "link-to-morley"));
    }

    @Test
    @SneakyThrows
    void aRelationshipWithNoPredicateDoesNotBreakSaving() {
        // JSON sent to the API need not include "relation"; Relationship's @JsonCreator does not
        // enforce @NonNull. It has no rule, so it is not checked - and must not be a 500.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = geminiRelating("dataset", null, "anything");

        //When / Then
        assertDoesNotThrow(() -> check(user, document));
    }

    @Test
    @SneakyThrows
    void aRestrictedRelationshipWithNoTargetIsRejected() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = geminiRelating("dataset", "http://purl.org/dc/terms/replaces", null);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );
    }

    @Test
    @SneakyThrows
    void aStoredLinkToTheSameTargetUnderAnotherRelationshipIsStillChecked() {
        // Stored relationships are matched on relation AND target. Matching the target alone would
        // let a record that is validly part of a collection X then claim it was produced at X.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument stored = geminiRelating("dataset", "http://purl.org/dc/terms/isPartOf", "a-collection");
        given(documentBundleReader.readBundle("cosmos")).willReturn(stored);
        GeminiDocument incoming = (GeminiDocument) new GeminiDocument()
            .setType("dataset")
            .setMetadata(MetadataInfo.builder().build());
        incoming.setRelationships(Set.of(
            new Relationship("http://purl.org/dc/terms/isPartOf", "a-collection"),
            new Relationship(UTILISES, "a-collection")
        ));
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("a-collection")).willReturn(new GeminiDocument().setType("aggregate"));

        //When / Then
        assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, incoming)
        );
    }

    @Test
    @SneakyThrows
    void aSameTypeRelationshipFromATypelessRecordSaysSo() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = geminiRelating(null, "http://purl.org/dc/terms/replaces", "old-version");
        given(documentIdentifierService.getBaseUri()).willReturn(BASE_URI);
        given(documentBundleReader.readBundle("cosmos")).willReturn(null); // a create: nothing stored yet
        given(documentBundleReader.readBundle("old-version")).willReturn(new GeminiDocument().setType("dataset"));

        //When
        InvalidRelationshipTargetException thrown = assertThrows(
            InvalidRelationshipTargetException.class,
            () -> check(user, document)
        );

        //Then
        assertThat(thrown.getMessage())
            .contains("which links to a record of this document's own type");
    }
}
