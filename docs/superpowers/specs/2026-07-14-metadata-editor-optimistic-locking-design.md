# Spec: optimistic locking for the metadata editor

- **Status:** Approved for planning
- **Date:** 2026-07-14
- **Design note:** `docs/metadata-editor-optimistic-locking.md` (problem, precedent, rationale)
- **Related:** `docs/datastore-performance-design.md` § Read-your-own-writes (this is the cross-referenced defence-in-depth measure)
- **Precedent:** proxy repo `uk.ac.ceh.portals` commit `263d321`

## Goal

Prevent one editor from silently overwriting another's concurrent edit to the
same metadata record (a lost update). Today `AbstractDocumentController.
saveMetadataDocument` does an unconditional read-modify-write with no check
that the record changed since the editor loaded it.

Adopt the optimistic-locking pattern the proxy already ships, adapted for the
catalogue's scale (8,000+ independent documents, not one shared config file):
a **per-document** Git revision token, delivered over HTTP `ETag`/`If-Match`,
enforced by a `synchronized` compare-then-commit at the application layer. No
change to the shared `datastore-git` library.

## Decisions (settled during brainstorming)

| # | Decision |
|---|----------|
| 1 | **Scope: full-stack** — Java backend + Backbone editor, end-to-end working feature. |
| 2 | **Token: per-document**, from `DataRepository.getRevisions(name)`'s newest entry (last commit touching *this* file). Not the repo-wide `getLatestRevisionId()` (would cause spurious cross-record conflicts at scale). |
| 3 | **Missing `If-Match`: mandatory — reject with `428 Precondition Required`.** Stronger guarantee than best-effort; migration implication for non-editor callers is flagged below. |
| 4 | **Stale `If-Match`: `409 Conflict`**, response body carries the submitted-but-unsaved document so the editor loses nothing. |
| 5 | **Conflict UX: banner + preserve edits** (proxy-style). Warning banner, keep the user's in-progress edits, require explicit reload before saving again. No diff view. |
| 6 | **Lock: single global write lock** (`synchronized`) spanning read-token → compare → commit. Safe because writes are rare/light (per datastore RFC) and matches the proxy. |

## Scope

**In scope:** the metadata-editor save path — every PUT that funnels through
`AbstractDocumentController.saveMetadataDocument` (Gemini, all Monitoring
types, CehModel, CehModelApplication, DataType, LinkDocument via
`DocumentController`; plus `CatalogueDocumentController`,
`NercModelController`, `CodeDocumentController`,
`InfrastructureRecordController`), and the corresponding GET endpoints that
must now emit an `ETag`.

**Out of scope:** the clone POST, permission-change endpoints, CFF harvest,
and service-agreement writes (`GitRepoServiceAgreementService`, which uses the
separate `readAtRevision` historical path). Diff-based conflict resolution in
the editor (possible follow-up).

## Backend design

### 1. Per-document revision token

Add to `CachedDataRepository`:

```java
public static final String DOC_REVISION_CACHE = "datastore-doc-revision";

@Cacheable(value = DOC_REVISION_CACHE, key = "#name")
public String getDocumentRevisionId(String name) throws IOException {
    List<DataRevision<CatalogueUser>> revisions = repo.getRevisions(name);
    return revisions.isEmpty() ? null : revisions.get(0).getRevisionID();
}
```

- `getRevisions(name)` is a path-scoped `git log`, newest-first (confirmed on
  `GitDataRepository`, an interface method on `DataRepository`). Element 0 is
  the last commit that touched this file.
- Cached by name, mirroring `readLatest`. Evicted on save/delete — see below.
- The document id (without extension) is the natural key; internally the token
  should reflect a change to **either** `.meta` or `.raw`. Since
  `GitRepoWrapper.save()` commits both in a single commit, a path-scoped log on
  either file returns the same latest commit, so keying on one file
  (`<id>.meta`) is sufficient and correct. The plan must verify this assumption
  with a real-repo test.

### 2. Eviction

Extend the existing `@Caching(evict = …)` on `GitRepoWrapper.save` **and**
`GitRepoWrapper.delete` (and `CachedDataRepository.evictAfterDirectWrite` for
consistency, though the SA path is out of scope) to also evict
`DOC_REVISION_CACHE` for the affected id. This keeps the token cache coherent
with the same contract already governing `LATEST_CACHE`.

### 3. ETag on read

`DocumentController.readMetadata` (and the other in-scope document GET
endpoints) currently return `MetadataDocument` with `@ResponseBody`. Change to
`ResponseEntity<MetadataDocument>`, setting `.eTag("\"" + revisionId + "\"")`
from `cachedRepo.getDocumentRevisionId(file + ".meta")`. Preserve existing
behaviour (Jena relationship population, metrics recording, link
post-processing) — only the wrapping changes.

A `null` token (document has no history yet — should not happen for an
existing record, but defensively) means no `ETag` header is emitted.

### 4. If-Match on write + compare-then-commit

- `AbstractDocumentController.saveMetadataDocument` gains an `ifMatch`
  parameter, sourced from the `If-Match` request header at each PUT mapping
  (`@RequestHeader(value = HttpHeaders.IF_MATCH, required = false)`).
- **Missing header → throw `MetadataPreconditionRequiredException` → 428.**
- The compare-then-commit lives in `GitDocumentRepository` (the private
  `save(...)` that all overloads funnel into), inside a `synchronized` block on
  the write-lock monitor:
  1. Re-read `getDocumentRevisionId(id + ".meta")` fresh (bypassing any stale
     view — it reads through the cache, which is evicted on every commit, so it
     reflects the latest commit).
  2. Compare (unquoted) to the submitted `If-Match` value.
  3. Mismatch → throw `MetadataConflictException` (carrying the submitted
     document) → 409.
  4. Match → proceed to `repo.save(...)` exactly as today.
- The `synchronized` block must wrap read-check-write as a unit (TOCTOU: two
  concurrent saves must not both pass the check before either commits). Since
  `GitRepoWrapper.save` is the single write funnel and writes are light, a
  single shared monitor is acceptable.

### 5. Exception → status mapping

A `@RestControllerAdvice` (or additions to the existing global handler) maps:
- `MetadataPreconditionRequiredException` → `428 Precondition Required`, body: a
  short problem message.
- `MetadataConflictException` → `409 Conflict`, body: **the submitted
  (unsaved) document**, so the client retains the user's edit, plus a message
  and the current server-side revision for context.

### 6. New/create (POST) path

`saveNewMetadataDocument` (POST, no prior version) does **not** require
`If-Match` — there is nothing to conflict with. The 428 rule applies to the
**PUT** update path only.

## Frontend design (`web/src/editor/`)

`EditorMetadata.js` already overrides Backbone `sync` — the single hook needed:

- **Capture ETag on read:** in the `sync` wrapper, on a successful `read`,
  capture the response `ETag` header (via the jqXHR in a `success`/`complete`
  shim) and store it on the model (e.g. `this._revision`).
- **Send If-Match on update:** on `update`, add
  `headers: { 'If-Match': this._revision }` to the sync options.
- **Handle 409:** on save error with status 409, render a conflict banner
  ("This record was changed by another user since you opened it. Your changes
  are preserved — reload to see the latest version before saving."), keep the
  user's in-progress edits in the form, and block further saves until an
  explicit reload. On reload, the fresh GET supplies a new ETag and normal
  saving resumes.
- **Handle 428:** should not occur from the editor (it always sends the header
  after a successful load); if it does, surface as a generic save error.
- Refresh `this._revision` from the ETag on the successful save response so a
  multi-save session never self-conflicts.

## Migration / rollout implication (must be surfaced pre-deploy)

Decision 3 (mandatory 428) means **any existing non-editor caller of the
in-scope PUT endpoints that does not send `If-Match` will start being rejected
with 428.** The plan must include a pre-deploy checklist item: identify such
callers (scripts, API integrations, harvesters that PUT documents) and update
or coordinate them before this ships. This is a deliberate behaviour change,
not a silent one.

## Testing

Mirror the proxy's `263d321` test structure:

- **Real-repo test** (`GitDataRepository`-backed): the per-document token
  changes when *this* document is committed, and does **not** change when an
  unrelated document is committed. Proves the per-document (not repo-wide)
  semantics. Also verifies keying on `<id>.meta` reflects a `.raw`-only change
  (both committed together).
- **Service-level test:** `GitDocumentRepository.save` with a stale token
  throws `MetadataConflictException`; with a matching token, saves normally.
- **Controller tests:** PUT with no `If-Match` → 428; PUT with stale `If-Match`
  → 409 and the response body echoes the submitted document; PUT with current
  `If-Match` → 200 and a new `ETag`. GET → 200 with an `ETag`.
- **Frontend (Karma/Jasmine):** editor sends `If-Match` on update; a 409
  response renders the banner and preserves edits; the model's stored revision
  updates from the save response ETag.
- Run `./gradlew :java:test` and, for the web changes, `cd web && npm run test`
  and `npm run standard`.

## Acceptance criteria

- Two saves to the same record, the second based on a no-longer-current
  revision, → the second is rejected with `409`, not silently applied.
- The rejected save's content is returned in the 409 body (not lost).
- A PUT with no `If-Match` → `428`.
- A normal single-editor session (repeated saves, each carrying the previous
  save's returned ETag) never spuriously conflicts.
- A save to record Y does **not** cause a conflict for a concurrently-open
  editor on unrelated record X.
- All new/changed behaviour covered by the tests above; full `:java:test` and
  web test suites pass.
