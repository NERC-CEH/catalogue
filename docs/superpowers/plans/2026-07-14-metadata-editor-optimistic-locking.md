# Metadata Editor Optimistic Locking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop one editor silently overwriting another's concurrent edit to the same metadata record, by adding per-document optimistic locking over HTTP `ETag`/`If-Match`.

**Architecture:** A per-document Git revision (the newest commit touching that document, via `DataRepository.getRevisions(name)`) is the lock token. `GET documents/{file}` returns it as an `ETag`; the editor sends it back as `If-Match` on `PUT`. A `synchronized` compare-then-commit in `GitRepoWrapper.save` rejects a stale token with `409 Conflict` (echoing the unsaved document back) and a missing token with `428 Precondition Required`. No change to the shared `datastore-git` library.

**Tech Stack:** Java 25 / Spring Boot (JUnit 5 + Mockito + BDDMockito, `@ExtendWith(MockitoExtension.class)`); Backbone.js editor (`web/src/editor/`, Karma + Jasmine).

**Spec:** `docs/superpowers/specs/2026-07-14-metadata-editor-optimistic-locking-design.md`
**Design note:** `docs/metadata-editor-optimistic-locking.md`

## Global Constraints

- **Token is per-document, not repo-wide:** always `repo.getRevisions(name).get(0).getRevisionID()`, keyed on `<id>.meta`. Never `getLatestRevisionId()` (repo-wide HEAD) for the lock — that causes spurious cross-record conflicts.
- **Missing `If-Match` on PUT → `428`; stale `If-Match` → `409`.** The 409 body is the submitted (unsaved) document.
- **The `synchronized` block wraps read-token → compare → commit as one unit** (TOCTOU: two concurrent saves must not both pass the check before either commits).
- **The authoritative check reads the revision fresh** via `repo.getRevisions(...)` (uncached); the cached `getDocumentRevisionId` serves only the read-side `ETag`.
- **Scope is the editor PUT save path only.** The POST create path (`saveNew`) and internal callers (clone, upload, harvest) pass no expected revision and are not checked.
- **Testing:** Java changes → `./gradlew :java:test`; web changes → `cd web && npm run test` and `npm run standard`. Run both before declaring done (project CLAUDE.md).
- **Branch:** work on `feature/metadata-editor-optimistic-locking` (already created; `develop` is protected).

---

### Task 1: Per-document revision token on `CachedDataRepository`

**Files:**
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/CachedDataRepository.java`
- Test: `java/src/test/java/uk/ac/ceh/gateway/catalogue/repository/CachedDataRepositoryRevisionTest.java` (create)

**Interfaces:**
- Consumes: `DataRepository.getRevisions(String name)` → `List<DataRevision<CatalogueUser>>`, newest-first; `DataRevision.getRevisionID()` → `String`.
- Produces: `CachedDataRepository.getDocumentRevisionId(String name)` → `String` (nullable); constant `CachedDataRepository.DOC_REVISION_CACHE = "datastore-doc-revision"`.

- [ ] **Step 1: Write the failing test** (real `GitDataRepository` on a temp dir — proves per-document semantics)

Create `CachedDataRepositoryRevisionTest.java`:

```java
package uk.ac.ceh.gateway.catalogue.repository;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.io.File;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CachedDataRepositoryRevisionTest {

    private DataRepository<CatalogueUser> gitRepo(File dir) throws Exception {
        return new GitDataRepository<>(dir, CatalogueUser::new, new EventBus());
    }

    private void commit(DataRepository<CatalogueUser> repo, String id, String body) throws Exception {
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        repo.submitData(id + ".meta", o -> o.write(("meta-" + body).getBytes()))
            .submitData(id + ".raw", o -> o.write(("raw-" + body).getBytes()))
            .commit(user, "commit " + id + " " + body);
    }

    @Test
    public void tokenChangesOnlyWhenThisDocumentChanges(@TempDir Path tmp) throws Exception {
        // Given a real git-backed store with two documents committed
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commit(repo, "docA", "v1");
        commit(repo, "docB", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);

        String aBefore = cached.getDocumentRevisionId("docA.meta");
        assertThat(aBefore, is(notNullValue()));

        // When an UNRELATED document (docB) is committed
        commit(repo, "docB", "v2");

        // Then docA's token is unchanged (per-document, not repo-wide)
        assertThat(cached.getDocumentRevisionId("docA.meta"), is(aBefore));

        // And when docA itself changes, its token moves
        commit(repo, "docA", "v2");
        assertThat(cached.getDocumentRevisionId("docA.meta"), is(not(aBefore)));
    }

    @Test
    public void metaTokenReflectsRawOnlyChangeSinceBothCommitTogether(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commit(repo, "docA", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);
        String before = cached.getDocumentRevisionId("docA.meta");

        // commit() writes both .meta and .raw in one commit, as GitRepoWrapper.save does
        commit(repo, "docA", "v2");

        assertThat(cached.getDocumentRevisionId("docA.meta"), is(not(before)));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.repository.CachedDataRepositoryRevisionTest`
Expected: FAIL — `getDocumentRevisionId` does not exist (compile error).

- [ ] **Step 3: Add the method and cache constant to `CachedDataRepository`**

Add the constant beside the existing cache-name constants:

```java
    public static final String DOC_REVISION_CACHE = "datastore-doc-revision";
```

Add the imports `java.util.List` and `uk.ac.ceh.components.datastore.DataRevision`, then add the method:

```java
    /**
     * The per-document revision token used for optimistic locking: the id of the newest commit that
     * touched this file. Unlike {@link #getLatestRevisionId()} (repo-wide HEAD), this only changes when
     * this specific document changes, so an unrelated save does not trip a conflict. Cached by name and
     * evicted on save/delete, mirroring {@link #readLatest}. Returns null if the file has no history.
     */
    @Cacheable(value = DOC_REVISION_CACHE, key = "#name")
    public String getDocumentRevisionId(String name) throws IOException {
        List<DataRevision<CatalogueUser>> revisions = repo.getRevisions(name);
        return revisions.isEmpty() ? null : revisions.get(0).getRevisionID();
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.repository.CachedDataRepositoryRevisionTest`
Expected: PASS (both tests).

- [ ] **Step 5: Register the new cache** in `CacheConfig`

Find where the existing caches (`LATEST_CACHE`, `HISTORICAL_CACHE`, `REVISION_ID_CACHE`) are registered in `java/src/main/java/uk/ac/ceh/gateway/catalogue/config/CacheConfig.java` and add `CachedDataRepository.DOC_REVISION_CACHE` to the same list/set, following the exact pattern already there (Caffeine cache spec identical to `LATEST_CACHE`).

- [ ] **Step 6: Run the repository + config test packages**

Run: `./gradlew :java:test --tests "uk.ac.ceh.gateway.catalogue.repository.*" --tests "uk.ac.ceh.gateway.catalogue.config.*"`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/CachedDataRepository.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/config/CacheConfig.java \
        java/src/test/java/uk/ac/ceh/gateway/catalogue/repository/CachedDataRepositoryRevisionTest.java
git commit -m "feat: add per-document revision token to CachedDataRepository"
```

---

### Task 2: Conflict and precondition exceptions

**Files:**
- Create: `java/src/main/java/uk/ac/ceh/gateway/catalogue/model/MetadataConflictException.java`
- Create: `java/src/main/java/uk/ac/ceh/gateway/catalogue/model/MetadataPreconditionRequiredException.java`
- Test: `java/src/test/java/uk/ac/ceh/gateway/catalogue/model/MetadataConflictExceptionTest.java` (create)

**Interfaces:**
- Produces:
  - `new MetadataConflictException(String message, MetadataDocument submitted)`; `getSubmittedDocument()` → `MetadataDocument`. Extends `RuntimeException`.
  - `new MetadataPreconditionRequiredException(String message)`. Extends `RuntimeException`.

- [ ] **Step 1: Write the failing test**

Create `MetadataConflictExceptionTest.java`:

```java
package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class MetadataConflictExceptionTest {
    @Test
    public void carriesTheSubmittedDocument() {
        MetadataDocument doc = new GeminiDocument();
        MetadataConflictException ex = new MetadataConflictException("stale", doc);
        assertThat(ex.getMessage(), is("stale"));
        assertThat(ex.getSubmittedDocument(), is(sameInstance(doc)));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.model.MetadataConflictExceptionTest`
Expected: FAIL — class does not exist.

- [ ] **Step 3: Create the exception classes**

`MetadataConflictException.java`:

```java
package uk.ac.ceh.gateway.catalogue.model;

/**
 * Thrown when a metadata save is rejected because the document has changed since the editor loaded it
 * (optimistic-lock conflict). Carries the submitted-but-unsaved document so the caller can echo it back
 * to the client, preserving the user's in-progress edit. Maps to HTTP 409.
 */
public class MetadataConflictException extends RuntimeException {
    static final long serialVersionUID = 1L;
    private final transient MetadataDocument submittedDocument;

    public MetadataConflictException(String message, MetadataDocument submittedDocument) {
        super(message);
        this.submittedDocument = submittedDocument;
    }

    public MetadataDocument getSubmittedDocument() {
        return submittedDocument;
    }
}
```

`MetadataPreconditionRequiredException.java`:

```java
package uk.ac.ceh.gateway.catalogue.model;

/**
 * Thrown when a metadata update is attempted without the required If-Match precondition header, so no
 * optimistic-lock check can be performed. Maps to HTTP 428 Precondition Required.
 */
public class MetadataPreconditionRequiredException extends RuntimeException {
    static final long serialVersionUID = 1L;
    public MetadataPreconditionRequiredException(String message) {
        super(message);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.model.MetadataConflictExceptionTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/src/main/java/uk/ac/ceh/gateway/catalogue/model/MetadataConflictException.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/model/MetadataPreconditionRequiredException.java \
        java/src/test/java/uk/ac/ceh/gateway/catalogue/model/MetadataConflictExceptionTest.java
git commit -m "feat: add MetadataConflictException and MetadataPreconditionRequiredException"
```

---

### Task 3: Compare-then-commit in `GitRepoWrapper.save`

**Files:**
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/GitRepoWrapper.java`
- Test: `java/src/test/java/uk/ac/ceh/gateway/catalogue/repository/GitRepoWrapperTest.java`

**Interfaces:**
- Consumes: `DataRepository.getRevisions(String)`, `MetadataConflictException` (Task 2), `CachedDataRepository` constants (Task 1).
- Produces: overloaded `GitRepoWrapper.save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo, DataWriter dataWriter, String expectedRevision, MetadataDocument submittedForEcho)` — checks the token when `expectedRevision != null`, throws `MetadataConflictException` on mismatch, else performs the existing save. The existing 5-arg `save(...)` delegates with `expectedRevision = null` (no check).

- [ ] **Step 1: Write the failing tests**

Add to `GitRepoWrapperTest.java` (imports: `uk.ac.ceh.components.datastore.DataRevision`, `uk.ac.ceh.gateway.catalogue.model.MetadataConflictException`, `uk.ac.ceh.gateway.catalogue.model.MetadataDocument`, `java.util.List`, `org.junit.jupiter.api.assertThrows`, `given`, `mock`):

```java
    @Test
    public void savesWhenExpectedRevisionMatchesCurrent() throws Exception {
        //Given the current per-document revision is "rev1"
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        DataRevision<CatalogueUser> current = mock(DataRevision.class);
        given(current.getRevisionID()).willReturn("rev1");
        given(repo.getRevisions("doc1.meta")).willReturn(List.of(current));

        DataOngoingCommit commit = mock(DataOngoingCommit.class);
        DataRevision<CatalogueUser> newRev = mock(DataRevision.class);
        given(repo.submitData(eq("doc1.meta"), any())).willReturn(commit);
        given(commit.submitData(eq("doc1.raw"), any())).willReturn(commit);
        given(commit.commit(any(), any())).willReturn(newRev);
        given(newRev.getRevisionID()).willReturn("rev2");

        //When the caller's expected revision matches
        repoWrapper.save(user, "doc1", "msg", info, writer, "rev1", submitted);

        //Then the commit is performed
        verify(commit).commit(user, "msg");
    }

    @Test
    public void rejectsWhenExpectedRevisionIsStale() throws Exception {
        //Given the current per-document revision is "rev2" but the editor loaded "rev1"
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        DataRevision<CatalogueUser> current = mock(DataRevision.class);
        given(current.getRevisionID()).willReturn("rev2");
        given(repo.getRevisions("doc1.meta")).willReturn(List.of(current));

        //When/Then a conflict is raised and nothing is committed
        MetadataConflictException ex = assertThrows(MetadataConflictException.class, () ->
            repoWrapper.save(user, "doc1", "msg", info, writer, "rev1", submitted));
        assertThat(ex.getSubmittedDocument(), is(sameInstance(submitted)));
        verify(repo, never()).submitData(any(), any());
    }
```

Add imports for `assertThat`/`is`/`sameInstance`/`never` as needed.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapperTest`
Expected: FAIL — the 7-arg `save` overload does not exist.

- [ ] **Step 3: Add the checked overload and delegate the existing method**

In `GitRepoWrapper.java`, add imports for `MetadataConflictException`, `MetadataDocument`, `DataRevision`, `List`, `java.io.IOException`. Rename the current `save(...)` body into a new 7-arg method and make the 5-arg delegate:

```java
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo, DataWriter dataWriter) throws DataRepositoryException {
        save(user, id, message, metadataInfo, dataWriter, null, null);
    }

    @Caching(evict = {
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.LATEST_CACHE, key = "#id + '.raw'"),
        @CacheEvict(value = CachedDataRepository.DOC_REVISION_CACHE, key = "#id + '.meta'"),
        @CacheEvict(value = CachedDataRepository.REVISION_ID_CACHE, allEntries = true)
    })
    public void save(CatalogueUser user, String id, String message, MetadataInfo metadataInfo,
                     DataWriter dataWriter, String expectedRevision, MetadataDocument submittedForEcho) throws DataRepositoryException {
        // Guard read-check-write as one unit: two concurrent saves must not both pass the check before
        // either commits. The check reads the revision fresh (uncached) so it is always authoritative.
        synchronized (this) {
            if (expectedRevision != null) {
                String current = currentDocumentRevision(id);
                if (!expectedRevision.equals(current)) {
                    throw new MetadataConflictException(
                        "This record was changed by another user since you opened it.", submittedForEcho);
                }
            }
            Optional<MonitoringFacility> preUpdateFacility = facilityEventService.getMonitoringFacility(id);
            DataRevision<CatalogueUser> revision = repo.submitData(String.format("%s.meta", id), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
                .submitData(String.format("%s.raw", id), dataWriter)
                .commit(user, message);
            Optional<MonitoringFacility> postUpdateFacility = facilityEventService.getMonitoringFacility(id, revision.getRevisionID());
            facilityEventService.postRemovedEvent(preUpdateFacility, postUpdateFacility);
        }
    }

    private String currentDocumentRevision(String id) throws DataRepositoryException {
        List<DataRevision<CatalogueUser>> revisions = repo.getRevisions(id + ".meta");
        return revisions.isEmpty() ? null : revisions.get(0).getRevisionID();
    }
```

Keep the existing `delete(...)` method; add `DOC_REVISION_CACHE` eviction to its `@Caching(evict=...)` block too (key `#id + '.meta'`), matching the save method.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.repository.GitRepoWrapperTest`
Expected: PASS (existing `canSave` plus the two new tests).

- [ ] **Step 5: Commit**

```bash
git add java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/GitRepoWrapper.java \
        java/src/test/java/uk/ac/ceh/gateway/catalogue/repository/GitRepoWrapperTest.java
git commit -m "feat: add synchronised optimistic-lock check to GitRepoWrapper.save"
```

---

### Task 4: Thread the expected revision through `DocumentRepository` / `GitDocumentRepository`

**Files:**
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/DocumentRepository.java`
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/GitDocumentRepository.java`
- Test: `java/src/test/java/uk/ac/ceh/gateway/catalogue/repository/GitDocumentRepositoryTest.java` (add to it if it exists; else create)

**Interfaces:**
- Consumes: `GitRepoWrapper.save(user, id, message, metadataInfo, dataWriter, expectedRevision, submittedForEcho)` (Task 3).
- Produces: `DocumentRepository.save(CatalogueUser user, MetadataDocument document, String id, String message, String expectedRevision)` → `MetadataDocument`. The existing 4-arg `save(user, document, id, message)` delegates with `expectedRevision = null`.

- [ ] **Step 1: Write the failing test**

Add to `GitDocumentRepositoryTest.java` a test that a stale revision propagates as a conflict. If the test class doesn't exist, create it mirroring `GitRepoWrapperTest`'s Mockito style, mocking the collaborators (`GitRepoWrapper repo`, etc.) and stubbing `repo.save(...)` to throw `MetadataConflictException`:

```java
    @Test
    public void saveWithExpectedRevisionPropagatesConflict() throws Exception {
        //Given the wrapper rejects the save as a conflict
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        doThrow(new MetadataConflictException("stale", document))
            .when(repo).save(any(), eq("doc1"), any(), any(), any(), eq("rev1"), any());

        //When/Then saving with that stale revision surfaces the conflict
        assertThrows(MetadataConflictException.class, () ->
            documentRepository.save(user, document, "doc1", "Edited document: doc1", "rev1"));
    }
```

(Adjust mock/collaborator wiring to whatever the existing `GitDocumentRepository` constructor requires; use `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks` as elsewhere.)

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepositoryTest`
Expected: FAIL — 5-arg `save` does not exist.

- [ ] **Step 3: Add the 5-arg method to the interface**

In `DocumentRepository.java`, add:

```java
    MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String id,
        String message,
        String expectedRevision
    ) throws DocumentRepositoryException;
```

- [ ] **Step 4: Implement it in `GitDocumentRepository` and thread the token**

In `GitDocumentRepository.java`:
- Change the existing public `save(user, document, id, message)` to delegate: `return save(user, document, id, message, null);`
- Add the new 5-arg public method mirroring it but forwarding `expectedRevision`:

```java
    @Override
    public MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String id,
        String message,
        String expectedRevision
    ) throws DocumentRepositoryException {
        try {
            return save(user, document, retrieveMetadataInfoUpdatingRawType(document), id, message, expectedRevision);
        } catch (DocumentRepositoryException | IOException | PostProcessingException | UnknownContentTypeException ex) {
            throw new DocumentRepositoryException(
                String.format("Saving file: %s failed for user: %s", id, user.getUsername()), ex);
        }
    }
```
- Change the private `save(user, document, metadataInfo, id, message)` to take `expectedRevision` and pass it (plus the `document` for echo) to `repo.save`:

```java
    private MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        MetadataInfo metadataInfo,
        String id,
        String message,
        String expectedRevision
    ) throws DataRepositoryException, DocumentRepositoryException {
        updateIdAndMetadataDate(document, id);
        String uri = documentIdentifierService.generateUri(id);
        addRecordUriAsResourceIdentifier(document, uri);
        document.setUri(uri);
        validateUniqueResourceIdentifiers(document, id);
        repo.save(user, id, message, metadataInfo,
            (o) -> documentWriter.write(document, MediaType.APPLICATION_JSON, o),
            expectedRevision, document);
        return document;
    }
```
- Update the other internal callers of the private `save(...)` (the `InputStream` upload overload at ~line 139, the 4-arg and 3-arg public `save`, and `saveNew`) to pass `null` for `expectedRevision` — they are create/internal paths and are not lock-checked.

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.repository.GitDocumentRepositoryTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/DocumentRepository.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/repository/GitDocumentRepository.java \
        java/src/test/java/uk/ac/ceh/gateway/catalogue/repository/GitDocumentRepositoryTest.java
git commit -m "feat: thread expected revision through DocumentRepository.save"
```

---

### Task 5: Accept `If-Match` on PUT; require it (428 when absent)

**Files:**
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/AbstractDocumentController.java`
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentController.java` (every `saveMetadataDocument(...)` caller — the PUT mappings)
- Modify: the other in-scope controllers' PUT mappings that call `saveMetadataDocument`: `catalogue/CatalogueDocumentController.java`, `modelnerc/NercModelController.java`, `controllers/CodeDocumentController.java`, `infrastructure/InfrastructureRecordController.java`
- Test: `java/src/test/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentControllerTest.java` (add) and `AbstractDocumentController` behaviour via a controller test

**Interfaces:**
- Consumes: `DocumentRepository.save(user, document, id, message, expectedRevision)` (Task 4); `MetadataPreconditionRequiredException` (Task 2).
- Produces: `AbstractDocumentController.saveMetadataDocument(CatalogueUser user, String file, MetadataDocument document, String ifMatch)` — throws `MetadataPreconditionRequiredException` when `ifMatch` is null/blank; otherwise saves with the (unquoted) token.

- [ ] **Step 1: Write the failing test**

In `DocumentControllerTest.java`, add (mimicking existing controller-test wiring — `@Mock DocumentRepository documentRepository`, construct the controller):

```java
    @Test
    public void putWithoutIfMatchIsRejectedAsPreconditionRequired() {
        //Given an editor update with no If-Match header
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument doc = new GeminiDocument();

        //When/Then updating without the precondition header is refused
        assertThrows(MetadataPreconditionRequiredException.class, () ->
            controller.updateGeminiDocument(user, "doc1", doc, null));
    }

    @Test
    public void putWithIfMatchSavesWithThatRevision() throws Exception {
        //Given a matching read for the metadata graft and a stubbed save
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument doc = new GeminiDocument();
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read("doc1")).willReturn(existing);
        given(documentRepository.save(eq(user), eq(doc), eq("doc1"), any(), eq("rev1"))).willReturn(doc);

        //When updating with an If-Match
        controller.updateGeminiDocument(user, "doc1", doc, "\"rev1\"");

        //Then the (unquoted) revision is passed to the repository
        verify(documentRepository).save(eq(user), eq(doc), eq("doc1"), any(), eq("rev1"));
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.controllers.DocumentControllerTest`
Expected: FAIL — the PUT handlers do not accept an `If-Match` parameter.

- [ ] **Step 3: Update `AbstractDocumentController.saveMetadataDocument`**

```java
    @SneakyThrows
    protected ResponseEntity<MetadataDocument> saveMetadataDocument(
                    CatalogueUser user,
                    String file,
                    MetadataDocument document,
                    String ifMatch
    ) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new MetadataPreconditionRequiredException(
                "An If-Match header carrying the record's current revision is required to update it.");
        }
        String expectedRevision = unquoteETag(ifMatch);
        document.setMetadata(documentRepository.read(file).getMetadata());
        return ResponseEntity.ok(
            documentRepository.save(user, document, file, String.format("Edited document: %s", file), expectedRevision));
    }

    // ETag values are quoted per HTTP (e.g. "abc123"); the stored git revision is unquoted.
    private static String unquoteETag(String etag) {
        String trimmed = etag.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
```

Add the import for `MetadataPreconditionRequiredException`.

- [ ] **Step 4: Pass `If-Match` from every in-scope PUT mapping**

In `DocumentController.java`, add to each `update*` PUT handler a header parameter and forward it. Example for `updateGeminiDocument`:

```java
    public ResponseEntity<MetadataDocument> updateGeminiDocument(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody GeminiDocument document,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) {
        return saveMetadataDocument(user, file, document, ifMatch);
    }
```

Apply the identical change (add the `@RequestHeader(... IF_MATCH ...) String ifMatch` param, pass it to `saveMetadataDocument`) to every other PUT handler that calls `saveMetadataDocument`: `updateMonitoringActivity`, `updateMonitoringFacility`, `updateMonitoringNetwork`, `updateMonitoringProgramme`, `updateCehModelDocument`, `updateDataType`, `updateCehModelApplicationDocument`, `updateLinkDocument` (in `DocumentController`), and the corresponding PUT handlers in `CatalogueDocumentController`, `NercModelController`, `CodeDocumentController`, `InfrastructureRecordController`. Add `import org.springframework.http.HttpHeaders;` where missing.

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.controllers.DocumentControllerTest`
Expected: PASS. Fix any other controller tests that call the changed handler signatures (add a revision arg, as the proxy's `263d321` did for its controller tests).

- [ ] **Step 6: Commit**

```bash
git add java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/AbstractDocumentController.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentController.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/catalogue/CatalogueDocumentController.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/modelnerc/NercModelController.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/CodeDocumentController.java \
        java/src/main/java/uk/ac/ceh/gateway/catalogue/infrastructure/InfrastructureRecordController.java \
        java/src/test/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentControllerTest.java
git commit -m "feat: require If-Match on metadata PUT and pass revision to save"
```

---

### Task 6: Emit `ETag` on document GET

**Files:**
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentController.java` (`readMetadata`)
- Test: `java/src/test/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentControllerTest.java`

**Interfaces:**
- Consumes: `CachedDataRepository.getDocumentRevisionId(String)` (Task 1). Requires access to it in the controller — inject `CachedDataRepository` (or expose the value via `DocumentRepository`; prefer injecting `CachedDataRepository` since it already owns the token).
- Produces: `GET documents/{file}` → `ResponseEntity<MetadataDocument>` with `ETag: "<revision>"` when a revision exists.

- [ ] **Step 1: Write the failing test**

```java
    @Test
    public void getEmitsETagOfCurrentRevision() throws Exception {
        //Given a readable document and a known per-document revision
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument doc = new GeminiDocument();
        doc.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read("doc1")).willReturn(doc);
        given(cachedDataRepository.getDocumentRevisionId("doc1.meta")).willReturn("rev1");

        //When reading the document
        ResponseEntity<MetadataDocument> response = controller.readMetadata(user, "doc1", request);

        //Then the ETag carries the current revision (quoted per HTTP)
        assertThat(response.getHeaders().getETag(), is("\"rev1\""));
    }
```

Add a `@Mock CachedDataRepository cachedDataRepository` to the test and pass it into the controller constructor.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.controllers.DocumentControllerTest`
Expected: FAIL — `readMetadata` returns `MetadataDocument`, not `ResponseEntity`, and the controller has no `CachedDataRepository`.

- [ ] **Step 3: Inject `CachedDataRepository` and wrap the read response**

Add `CachedDataRepository` to the `DocumentController` constructor and store it. Change `readMetadata`:

```java
    @CrossOrigin
    @ResponseBody
    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("documents/{file}")
    public ResponseEntity<MetadataDocument> readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            HttpServletRequest request
        ) {
        MetadataDocument document = documentRepository.read(file);
        if (metricsService != null && !metricsExcludedUsers.contains(user.getUsername())
                && !document.getState().equals(GitRepoServiceAgreementService.DRAFT)) {
            metricsService.recordView(file, request.getRemoteAddr());
        }
        MetadataDocument body = postProcessLinkDocument(addJenaRelationships(document));
        String revision = cachedDataRepository.getDocumentRevisionId(file + ".meta");
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (revision != null) {
            builder.eTag(revision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(body);
    }
```

Update the `DocumentController` bean construction wherever it is wired (constructor call sites / `@Configuration`) to supply the new `CachedDataRepository` argument. Spring autowires it as an existing `@Service`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.controllers.DocumentControllerTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentController.java \
        java/src/test/java/uk/ac/ceh/gateway/catalogue/controllers/DocumentControllerTest.java
git commit -m "feat: emit ETag of current per-document revision on document GET"
```

---

### Task 7: Map the new exceptions to 428 and 409

**Files:**
- Modify: `java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/ExceptionControllerHandler.java`
- Test: `java/src/test/java/uk/ac/ceh/gateway/catalogue/controllers/ExceptionControllerHandlerTest.java` (add; create if absent)

**Interfaces:**
- Consumes: `MetadataConflictException` (with `getSubmittedDocument()`), `MetadataPreconditionRequiredException` (Task 2).
- Produces: HTTP `428` for the precondition exception; HTTP `409` for the conflict exception with the submitted document as the response body.

- [ ] **Step 1: Write the failing test**

```java
    @Test
    public void conflictExceptionMapsTo409WithSubmittedDocumentBody() {
        MetadataDocument submitted = new GeminiDocument();
        MetadataConflictException ex = new MetadataConflictException("stale", submitted);

        ResponseEntity<MetadataDocument> response = handler.handleMetadataConflict(ex);

        assertThat(response.getStatusCode(), is(HttpStatus.CONFLICT));
        assertThat(response.getBody(), is(sameInstance(submitted)));
    }

    @Test
    public void preconditionRequiredMapsTo428() {
        MetadataPreconditionRequiredException ex = new MetadataPreconditionRequiredException("need If-Match");
        ResponseEntity<Object> response = handler.handleMetadataPreconditionRequired(ex);
        assertThat(response.getStatusCode(), is(HttpStatus.PRECONDITION_REQUIRED));
    }
```

Construct `handler` as `new ExceptionControllerHandler(env)` with a mock `Environment`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.controllers.ExceptionControllerHandlerTest`
Expected: FAIL — handler methods do not exist.

- [ ] **Step 3: Add the two handlers**

In `ExceptionControllerHandler.java` (imports for the two exceptions and `MetadataDocument`):

```java
    @ExceptionHandler(MetadataConflictException.class)
    public ResponseEntity<MetadataDocument> handleMetadataConflict(MetadataConflictException ex) {
        // 409 with the submitted-but-unsaved document so the editor can preserve the user's edits.
        log.warn("Metadata save conflict: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(ex.getSubmittedDocument());
    }

    @ExceptionHandler(MetadataPreconditionRequiredException.class)
    public ResponseEntity<Object> handleMetadataPreconditionRequired(MetadataPreconditionRequiredException ex) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpStatus.PRECONDITION_REQUIRED);
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.controllers.ExceptionControllerHandlerTest`
Expected: PASS.

- [ ] **Step 5: Run the whole Java suite**

Run: `./gradlew :java:test`
Expected: PASS. Fix any controller tests elsewhere still calling old handler signatures.

- [ ] **Step 6: Commit**

```bash
git add java/src/main/java/uk/ac/ceh/gateway/catalogue/controllers/ExceptionControllerHandler.java \
        java/src/test/java/uk/ac/ceh/gateway/catalogue/controllers/ExceptionControllerHandlerTest.java
git commit -m "feat: map metadata conflict to 409 and missing precondition to 428"
```

---

### Task 8: Editor model — send `If-Match`, track the revision

**Files:**
- Modify: `web/src/editor/src/EditorMetadata.js`
- Test: `web/src/editor/test/EditorMetadataTest.js` (create)

**Interfaces:**
- Produces: `EditorMetadata` instances expose `setRevision(rev)` / `getRevision()`; on `update` sync they send `If-Match: <rev>`; on any successful sync they refresh the stored revision from the response `ETag`.

- [ ] **Step 1: Write the failing test**

Create `web/src/editor/test/EditorMetadataTest.js`:

```js
import EditorMetadata from '../src/EditorMetadata'
import Backbone from 'backbone'

describe('EditorMetadata optimistic locking', () => {
  let syncArgs
  beforeEach(() => {
    spyOn(Backbone, 'sync').and.callFake((method, model, options) => {
      syncArgs = { method, options }
      return { done: () => {} }
    })
  })

  it('sends If-Match header on update when a revision is set', () => {
    const model = new EditorMetadata({ id: 'doc1' })
    model.setRevision('rev1')
    model.set('id', 'doc1')
    model.save()
    expect(syncArgs.method).toBe('update')
    expect(syncArgs.options.headers['If-Match']).toBe('rev1')
  })

  it('does not send If-Match when no revision is set (create)', () => {
    const model = new EditorMetadata()
    model.save()
    const headers = syncArgs.options.headers || {}
    expect(headers['If-Match']).toBeUndefined()
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd web && npm run test`
Expected: FAIL — `setRevision` undefined / no `If-Match` header set.

- [ ] **Step 3: Implement in `EditorMetadata.js`**

```js
import Backbone from 'backbone'

export default Backbone.Model.extend({

  url () {
    return this.urlRoot()
  },

  urlRoot () {
    if (this.isNew()) {
      return `/documents?catalogue=${window.location.pathname.split('/')[1]}`
    } else {
      return `/documents/${this.id}`
    }
  },

  initialize (data, { mediaType = 'application/json' } = {}, title) {
    this.mediaType = mediaType
    this.title = title
    this._revision = null
  },

  setRevision (revision) {
    this._revision = revision
  },

  getRevision () {
    return this._revision
  },

  sync (method, model, options) {
    const headers = { ...(options.headers || {}) }
    if (method === 'update' && this._revision) {
      headers['If-Match'] = this._revision
    }
    const xhr = Backbone.sync.call(this, method, model, {
      ...options,
      headers,
      accepts: { json: this.mediaType },
      contentType: this.mediaType
    })
    // Refresh the stored revision from the response ETag so a multi-save session never self-conflicts.
    if (xhr && xhr.done) {
      xhr.done((data, status, jqXHR) => {
        const etag = jqXHR && jqXHR.getResponseHeader && jqXHR.getResponseHeader('ETag')
        if (etag) {
          this._revision = etag.replace(/^"|"$/g, '')
        }
      })
    }
    return xhr
  },

  validate (attrs) {
    const errors = []
    if (!attrs.title) {
      errors.push('A title is mandatory')
    }
    if (errors.length) {
      return errors
    }
  }
})
```

- [ ] **Step 4: Run test + lint to verify pass**

Run: `cd web && npm run test && npm run standard`
Expected: PASS, no lint errors.

- [ ] **Step 5: Commit**

```bash
git add web/src/editor/src/EditorMetadata.js web/src/editor/test/EditorMetadataTest.js
git commit -m "feat: editor model sends If-Match and tracks record revision"
```

---

### Task 9: Seed the revision from the load GET

**Files:**
- Modify: `web/src/index.js` (`initEditor`, the `$.ajax` load success)

**Interfaces:**
- Consumes: `EditorMetadata.setRevision(rev)` (Task 8); the load GET's `ETag` header.

- [ ] **Step 1: Capture the ETag from the load response and seed the model**

In `web/src/index.js` `initEditor`, the existing edit-load `$.ajax({... success(data) {...}})` gains the jqXHR argument and seeds the model. Change the success callback signature and body:

```js
        success (data, textStatus, jqXHR) {
          $('#metadata').removeClass('alert alert-danger missingResourceType')
          const model = new documentType.Model(data, documentType, title)
          const etag = jqXHR.getResponseHeader('ETag')
          if (etag) {
            model.setRevision(etag.replace(/^"|"$/g, ''))
          }
          new documentType.View({
            model,
            el: '#metadata'
          })
        }
```

(The create branch — `new documentType.Model(null, ...)` — is unchanged: no revision, POST create, no `If-Match`.)

- [ ] **Step 2: Lint**

Run: `cd web && npm run standard`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add web/src/index.js
git commit -m "feat: seed editor model revision from load response ETag"
```

---

### Task 10: Editor 409 conflict UX (banner, preserve edits, block re-save)

**Files:**
- Modify: `web/src/editor/src/EditorView.js`
- Test: `web/src/editor/test/EditorViewTest.js`

**Interfaces:**
- Consumes: the model `error` event with a `409` response.

- [ ] **Step 1: Write the failing test**

Add to `EditorViewTest.js` a spec asserting that a 409 error shows the conflict-specific messaging and does not clear the model's attributes (edits preserved). Mirror the existing test setup in that file (it already constructs an `EditorView` with a model). Example spec:

```js
  it('shows a conflict banner and preserves edits on 409', () => {
    const swalSpy = spyOn(Swal, 'fire')
    view.model.set('title', 'my in-progress title')

    // Simulate Backbone's error event for a 409 conflict
    view.model.trigger('error', view.model, { status: 409, statusText: 'Conflict', responseJSON: {} })

    expect(swalSpy).toHaveBeenCalled()
    const args = swalSpy.calls.mostRecent().args[0]
    expect(args.title.toLowerCase()).toContain('conflict')
    // edits are still on the model
    expect(view.model.get('title')).toBe('my in-progress title')
  })
```

(Import `Swal` into the test if not already; match the file's existing import style.)

- [ ] **Step 2: Run test to verify it fails**

Run: `cd web && npm run test`
Expected: FAIL — the generic error handler shows "Server response: 409 Conflict", not conflict-specific guidance.

- [ ] **Step 3: Special-case 409 in the model `error` handler**

In `EditorView.js` `initialize`, change the `this.listenTo(this.model, 'error', ...)` handler to branch on 409:

```js
    this.listenTo(this.model, 'error', function (model, response) {
      that.$('#editorAjax').toggleClass('visible')

      if (response && response.status === 409) {
        Swal.fire({
          title: 'Edit conflict',
          html: '<p>This record was changed by another user since you opened it. ' +
                'Your changes are shown below and have not been saved. ' +
                'Reload the record to see the latest version, then re-apply your changes.</p>',
          icon: 'warning',
          confirmButtonText: 'Close'
        })
        return
      }

      const message = response?.responseJSON?.message || response?.responseText || response?.statusText || 'There was a problem communicating with the server!'
      Swal.fire({
        title: `Server response: ${response.status} ${response.statusText}`,
        html: `<p>${_.escape(message)}</p><textarea readonly style="resize:none; height:auto;" rows="10">${JSON.stringify(model.toJSON())}</textarea>`,
        icon: 'error',
        confirmButtonText: 'Close'
      })
    })
```

The model's edits are held in its attributes and are not cleared on error, so "preserve edits" needs no extra code — the test asserts this. The `saveRequired` flag stays true (only the `sync` success handler clears it), so the user is still prompted about unsaved changes.

- [ ] **Step 4: Run test + lint to verify pass**

Run: `cd web && npm run test && npm run standard`
Expected: PASS, no lint errors.

- [ ] **Step 5: Commit**

```bash
git add web/src/editor/src/EditorView.js web/src/editor/test/EditorViewTest.js
git commit -m "feat: show conflict banner and preserve edits on 409 in editor"
```

---

### Task 11: Pre-deploy migration checklist + full-suite verification

**Files:**
- Modify: `docs/metadata-editor-optimistic-locking.md` (add a "Rollout checklist" section)

**Interfaces:** none (documentation + verification).

- [ ] **Step 1: Add the rollout checklist to the design note**

Append a section documenting the mandatory-428 migration implication:

```markdown
## Rollout checklist (mandatory 428)

Because a PUT without `If-Match` is now rejected with `428 Precondition
Required`, any non-editor caller of the metadata PUT endpoints must be
updated to send `If-Match` first:

- [ ] Identify non-browser callers of `PUT documents/{file}` (scripts,
      harvesters, API integrations) — search access logs / integration code.
- [ ] Update each to `GET` the record, read the `ETag`, and send it as
      `If-Match` on `PUT`.
- [ ] Announce the behaviour change to API consumers before deploy.
- [ ] Confirm the browser editor path works end to end in staging (save,
      concurrent-save conflict, reload-and-retry).
```

- [ ] **Step 2: Run the full backend suite**

Run: `./gradlew :java:test`
Expected: PASS (all).

- [ ] **Step 3: Run the full web suite and lint**

Run: `cd web && npm run test && npm run standard`
Expected: PASS, no lint errors.

- [ ] **Step 4: Manual end-to-end check (staging or local docker compose)**

Verify against a running app (per CLAUDE.md — don't declare success on edits alone):
1. Open a record in the editor, save → succeeds, save again → still succeeds (revision refreshes).
2. Open the same record in two tabs; save in tab 1; save in tab 2 → conflict banner, edits preserved.
3. Reload tab 2, re-apply, save → succeeds.
4. `curl -X PUT` the record with no `If-Match` → `428`.

- [ ] **Step 5: Commit**

```bash
git add docs/metadata-editor-optimistic-locking.md
git commit -m "docs: add optimistic-locking rollout checklist"
```

---

## Self-review notes

- **Spec coverage:** token (T1), exceptions (T2), compare-then-commit + lock scope (T3), threading (T4), 428/If-Match (T5), ETag on read (T6), status mapping incl. 409-with-body (T7), editor If-Match + revision tracking (T8/T9), 409 UX (T10), migration checklist + full verification incl. per-document acceptance criterion exercised in T1 and the concurrent-conflict E2E in T11. All spec sections map to a task.
- **Token naming consistent:** `getDocumentRevisionId(name)` (cached, read side) and `currentDocumentRevision(id)` (fresh, check side) both resolve `getRevisions(...).get(0).getRevisionID()`; both keyed on `<id>.meta`.
- **428 vs 409 consistent** across T5 (throw), T7 (map), T10/T11 (consume) — missing → 428, stale → 409.
