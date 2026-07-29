# Design: optimistic locking for the metadata editor (prevent lost updates)

- **Status:** Draft for discussion
- **Date:** 2026-07-14
- **Area:** Metadata editor save path (`documents/{file}` PUT), concurrent-edit protection

## How to use this document

This is a design note, not an implementation-ready specification. It records
the **problem**, the **precedent** this is adapted from, a **design**, and
the one open question that most affects whether the design works at this
repo's scale. It does not prescribe a build order.

## Summary

Two editors working on the same catalogue record concurrently can silently
overwrite each other today — the save path does an unconditional
read-modify-write, with no check that anything changed since the editor
started editing. The proxy repo (`uk.ac.ceh.portals`) fixed the identical bug
class for its config editors (commit `263d321`, *"Prevent lost updates on the
proxy-endpoints and licences editors"*) using optimistic locking keyed on the
Git commit revision. This note proposes the same pattern here, adapted for
the catalogue's very different scale: one shared config file at the proxy,
versus 8,000+ independent documents in the catalogue's datastore.

## Problem

Traced end to end (see `docs/datastore-performance-design.md` § Appendix §
Read-your-own-writes for the fuller trace): `AbstractDocumentController.
saveMetadataDocument` reads the record's current `MetadataInfo`, grafts it
onto the incoming edit, and unconditionally commits — no check that the
record hasn't moved on since the editor loaded it.

**Failure mode:** Editor A and Editor B both open record X. A edits and
saves. B, unaware of A's change, edits (from their now-stale copy) and
saves — B's save overwrites A's edit entirely, with no warning, no conflict,
no trace beyond a git history nobody is watching. This is a classic lost
update.

**Related but distinct:** `docs/datastore-performance-design.md` §
Constraints and risks § Read-your-own-writes documents an adjacent risk —
a stale local-mirror read silently reverting a metadata change on the next
save, if Option C were ever scoped to route `CachedDataRepository` through a
periodically-refreshed mirror. That RFC's fix (keep `CachedDataRepository`
on the always-fresh primary repo) already prevents that specific scenario.
Optimistic locking, described here, is a second, independent safety net —
it doesn't replace that fix, but it would also catch that class of
staleness (and any other), as a side effect of catching lost updates in
general.

## Precedent: the proxy repo's fix (commit `263d321`)

- `getCurrentRevision()` returns `repository.getLatestRevision().
  getRevisionID()` — the Git commit SHA of HEAD. The commit message flags
  the obvious-looking trap: `getData(name).getRevision()` returns the
  literal ref string it was queried with (e.g. `"HEAD"`) and never changes,
  so it cannot be used as a lock token.
- The revision is handed to the editor on load (a hidden form field, since
  the proxy renders server-side FreeMarker forms) and posted back on save.
- `updateConfigurationFile` performs a **`synchronized` compare-then-commit**:
  re-read the current revision fresh, compare to what was submitted;
  mismatch → throw `ConfigurationConflictException` instead of writing.
- On conflict, the controller re-renders the page with the user's edits
  intact and the *stale* revision preserved, so a blind re-submit conflicts
  again rather than silently succeeding.
- The `synchronized` block wraps the whole read-check-write, not just the
  comparison — otherwise two near-simultaneous saves could both read the
  same "current" revision and both pass the check before either commits.
- The fix lives entirely in the proxy's own application code
  (`GitConfigurationStorageService`), not in the shared `datastore-git`
  library — it just calls methods already on `DataRepository`, the same
  interface `GitDataRepository<CatalogueUser>` implements here.

## Design

**Token:** `CachedDataRepository.getLatestRevisionId()` already computes
`repo.getLatestRevision().getRevisionID()` here — currently used only to key
the `readLatest` byte cache. No new plumbing needed to get the *value*; see
Open question below for whether this specific value is the *right* token at
this scale.

**HTTP mechanism:** standard `ETag`/`If-Match`, not a hidden field — the
catalogue's editor is a Backbone JSON/REST SPA, unlike the proxy's
server-rendered forms:
- `GET documents/{file}` responses include `ETag: "<revisionId>"`.
- `PUT documents/{file}` carries `If-Match: "<revisionId>"`.

**Compare-then-commit**, added at the application layer (`GitRepoWrapper`/
`GitDocumentRepository`, mirroring the proxy's choice not to touch the
shared library):
1. Inside a `synchronized` block (same granularity as `GitDataRepository.
   submit()`, which is already `synchronized`), re-read the current
   revision token fresh.
2. Compare to the submitted `If-Match`.
3. Mismatch → throw a new `MetadataConflictException`, mapped to HTTP `409
   Conflict`, with the submitted (unsaved) document echoed back in the
   response body so the editor's in-progress edit isn't lost.
4. Match → proceed with the existing save.

**Client-side (Backbone editor):** capture `ETag` on load, send it as
`If-Match` on save; on `409`, show a conflict banner (same spirit as the
proxy's) and keep the user's edits in the form; each subsequent save in a
session carries the previous save's *new* revision, so a normal
single-editor session never spuriously conflicts with itself.

## Open question: repo-wide vs. per-document revision (the key scale adaptation)

**This is the one place the proxy's approach does not transfer directly,**
and it should be resolved before implementation, not left as a footnote.

The proxy has exactly one config file — repo-wide HEAD *is* that file's
revision, so there's no distinction to make. The catalogue's datastore holds
8,000+ independent documents. If the lock token is the **repo-wide** HEAD
revision (the direct copy of the proxy's approach), then *any* save
anywhere in the entire catalogue — by any editor, on any unrelated record —
bumps the token and trips every other currently-open editor's conflict
check, even though nobody touched their record. Given editors already "keep
saving the same document multiple times throughout a session" and multiple
editors work different records concurrently, a repo-wide token would produce
frequent spurious conflicts between completely unrelated saves — disruptive
UX, and not what "prevent one editor overwriting another" is actually
asking for.

**Recommendation: use a per-document revision token instead.**
`GitDataRepository.getRevisions(name)` already exists (a `git log` scoped to
that file's path, newest-first) — the first entry's revision ID is the last
commit that touched *this specific file*, giving a token that only changes
when the record itself changes. This costs one extra path-scoped log call
per document read (more than a bare HEAD lookup, materially less than the
content reads it would sit alongside), and — like `readLatest` — can be
cached by name and evicted on save.

This means the token exposed as `ETag` should be **per-document**
(`getRevisions(name)` head), not the repo-wide `getLatestRevisionId()` used
elsewhere in this codebase for cache-keying. The two serve different
purposes and should not be conflated.

**Resolved, with a correction (see Constraints below).** A per-document token
is right, but it cannot be read from *one* of the document's two files. The
token is the pair — `"<.meta revision>:<.raw revision>"` — computed by
`CachedDataRepository.getDocumentRevisionToken(documentId)`.

## Constraints and risks

- **Lock scope:** the `synchronized` block must wrap the full
  read-check-write as a unit, exactly as the proxy does — narrowing it to
  just the comparison reopens the TOCTOU race between two concurrent saves.
- **Multi-file documents:** each document is two files (`.raw` + `.meta`).
  The per-document token must reflect a change to *either*.

  The tempting shortcut — "the write path commits them together, so watching
  one file is enough" — is **wrong**, and quietly so. `getRevisions(name)` is
  `git log -- <path>`, which applies ANY_DIFF history simplification: a commit
  is only in a path's log if that path's *content* changed. Committing two
  files together does not put both in both logs. A content edit rewrites
  `.raw` but re-serialises `.meta` byte-identical, so git sees no diff for
  `.meta` and the `.meta` log does not advance; a permissions edit is the
  mirror image. Watching one file therefore misses every edit to the other —
  and a lock token that fails to move fails *open*, silently permitting the
  lost update it exists to prevent.

  The token is consequently the pair, `"<.meta revision>:<.raw revision>"`,
  compared by string equality. Cost is two path-scoped log walks instead of
  one; cached per document id and evicted on write, so it is paid on cache
  miss and once per save. `CachedDataRepositoryRevisionTest` pins all three
  cases (body-only, permissions-only, unrelated document) against a real git
  repository — mocking `getRevisions` cannot catch this class of bug, because
  the bug *is* the real library's path-filter semantics.
- **Service-agreement's versioned read path** (`readAtRevision`, explicit
  historical revisions) is unaffected — this note only concerns the
  "latest" editor save path.
- **Interaction with Option C** (`docs/datastore-performance-design.md`):
  the per-document token must always be read from the primary repository,
  never a mirror, for the same reason `CachedDataRepository` must stay off
  the mirror — a stale token would either falsely conflict (harmless, just
  annoying) or, worse, falsely *match* a revision that's actually moved on
  (defeats the whole point). This note's mechanism and that RFC's mirror
  scoping should be implemented consistently, not independently.
- **Mandatory vs. best-effort `If-Match`:** DECIDED — **mandatory**. A PUT to
  a protected endpoint without `If-Match` is rejected with `428 Precondition
  Required`. This gives the strongest guarantee (no client can blind-write)
  at the cost of a migration for any non-editor caller (see Rollout
  checklist).

## Acceptance criteria

- Two saves to the same record, the second based on a revision that is no
  longer current, are rejected with `409`, not silently applied.
- The rejected save's content is not lost — returned in the response body.
- A normal single-editor session (repeated saves, each carrying the
  previous save's returned revision) never spuriously conflicts.
- A save to record Y does **not** cause a concurrently-open editor on
  unrelated record X to conflict (the repo-wide-token failure mode this
  note steers away from).
- Test coverage mirrors the proxy's structure (commit `263d321`): a
  real-repo test proving the token changes when, and only when, the
  specific document changes; service-level conflict tests; controller
  conflict-path tests.

## Rollout checklist (mandatory 428)

Because a PUT without `If-Match` is now rejected with `428 Precondition
Required`, any non-editor caller of the protected PUT endpoints must be
updated to send `If-Match` first. The protected endpoints are:

- `PUT documents/{file}` (all metadata-editor document types)
- `PUT documents/{file}/permission`
- `PUT documents/{file}/catalogue` and `PUT documents/{file}/catalogue-view`

Checklist:

- [ ] Identify non-browser callers of those PUT endpoints (scripts,
      harvesters, API integrations) — search access logs / integration code.
- [ ] Update each to `GET` the record, read the `ETag`, and send it as
      `If-Match` on `PUT`.
- [ ] Announce the behaviour change to API consumers before deploy.
- [ ] Confirm the browser editor paths work end to end in staging — for the
      document editor, the permission editor, and the catalogue /
      catalogue-view panels: save, concurrent-save conflict, reload-and-retry.

## Coverage: which write paths are protected

Optimistic locking guards the write paths that have a genuine
load-edit-save **editing session** — where a client holds a revision across
time and can race another writer. Each funnels through the one shared
mechanism (`DocumentRepository.save(..., expectedRevision)` →
`GitRepoWrapper`'s synchronised fresh-read check); the shared
`IfMatchRevision.require` helper parses the header uniformly.

This section describes the **interactive editor endpoints** that were
brought under this mechanism. It is illustrative, not an exhaustive audit of
every `DocumentRepository.save` caller in the codebase — the repository has
many server-side/programmatic writers beyond the browser-facing editors, and
not all of them are protected.

**Protected (If-Match required, 409 on stale):**

- `AbstractDocumentController.saveMetadataDocument` — the main metadata
  editor (all document types).
- `PermissionController.updatePermission` — the permission editor. The
  highest-value case: a long-lived session that does a *wholesale* replace
  of the permission set, with a concrete concurrent actor (service-agreement
  depositor permission grants touch the same document's permissions).
- `CatalogueDocumentController` — `PUT {file}/catalogue` and
  `{file}/catalogue-view`. Narrower window (the server re-reads fresh within
  the same request and the client only holds the catalogue value, not
  document content), protected for consistency.
- `ServiceAgreementController` — `PUT {id}` (the service-agreement editor)
  and `PUT {id}/permission`. A depositor fills the service agreement in over
  a long session and saves repeatedly, so it is an editing session in exactly
  the sense above. It does not go through `DocumentRepository`: the service
  commits directly to the datastore, so the compare-then-commit lives in
  `GitRepoServiceAgreementService` instead, guarding the same read-check-write
  under its own `synchronized` block. The client side needed no change — the
  editor model extends `EditorMetadata` and the permission page uses the
  shared `Permission` model, so both already send `If-Match` and handle 409.

**Other writers using the unchecked `DocumentRepository.save(...)`
(known examples, not a complete list):**

- `DocumentPublicationService.transition` (publish/withdraw) and
  `GitRepoServiceAgreementService.publishServiceAgreement` (as distinct from
  the service-agreement *editor*, which is protected — see above). These are
  **one-shot server-rendered workflow transitions** — a full-page HTML
  `<form method="post">` button, with no editing session and no channel to
  carry an `If-Match` header without inventing hidden-field plumbing. Each
  reads the document fresh and writes within a single service-method call,
  so the lost-update window is effectively just the request duration, not a
  human editing session. Adding optimistic locking here would be awkward
  plumbing for negligible real-world benefit; if these ever gain an
  interactive editing surface, revisit. (`GitRepoServiceAgreementService`
  uniquely rewrites full document *content* from the service agreement, so
  if a collision ever proves real in practice, it is the more likely of the
  two to warrant revisiting.)
- `UploadController.removeUploadPermission` — a housekeeping write
  triggered as part of the upload/hubbub flow, not a user-facing editing
  session.
- `NetworkIndexingService.updateBoundingBox` — mutates a `MonitoringNetwork`
  document, which **is** editor-visible, so this one can genuinely race a
  concurrent main-editor session on the same network document. Of the
  writers listed here, it is the more plausible future candidate for
  locking if a real collision ever surfaces.

Treat this list as a starting point: before relying on "protected" vs "not
locked" for a specific document type or endpoint, grep for
`DocumentRepository.save` callers on that path and check for yourself.
