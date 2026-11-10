# API Documentation
A REST API to create, view, modify and delete metadata records.

Replace curly brackets {} with appropriate content.

### Authentication
All requests authenticate with a **personal access token** sent as a Bearer
token:

    Authorization: Bearer {personal access token}

For example `Authorization: Bearer glpat-xxxxxxxxxxxxxxxxxxxx`.

### Defined Values
[CatalogueServiceConfig](../java/src/main/java/uk/ac/ceh/gateway/catalogue/config/CatalogueServiceConfig.java) for valid values of {catalogue identifier}

{document media type} is the record type's own JSON media type: one of the `*_JSON_VALUE` constants in
[CatalogueMediaTypes](../java/src/main/java/uk/ac/ceh/gateway/catalogue/CatalogueMediaTypes.java),
e.g. `application/gemini+json` or `application/vnd.monitoring-facility+json`. Only the types listed in
`RECORD_JSON_MEDIA_TYPES` in [WebConfig](../java/src/main/java/uk/ac/ceh/gateway/catalogue/config/WebConfig.java)
are accepted; any other `+json` type is rejected with `406` or `415`.

### File Identifier
The jsonpath of {identifier} from the response payload is

    $.id

## Create

    POST /documents?catalogue={catalogue identifier}

    Accept: application/json
    Authorization: Bearer {personal access token}
    Content-Type: {document media type}

    {JSON request payload}

## View

    GET /documents/{identifier}

    Accept: application/json
    Authorization: Bearer {personal access token}

To get a dataset's schema.org description as JSON-LD, send `Accept: application/ld+json`
(optionally with `profile="https://schema.org"`). This is available for Gemini records only;
other record types return `406 Not Acceptable`.

## Modify

    PUT /documents/{identifier}

    Accept: application/json
    Authorization: Bearer {personal access token}
    Content-Type: {document media type}
    If-Match: "{revision}"

    {JSON request payload}

The `If-Match` header is **required** on every modify request — see
[Concurrency control](#concurrency-control-optimistic-locking) below. A
request without it is rejected with `428 Precondition Required`; a request
carrying a revision that is no longer current is rejected with `409
Conflict`.

## Delete

    DELETE /documents/{identifier}
    Authorization: Bearer {personal access token}

## Concurrency control (optimistic locking)

Modifying a record uses **optimistic locking** so that two people (or two
scripts) editing the same record cannot silently overwrite each other. It
follows the standard HTTP `ETag` / `If-Match` mechanism — you never invent or
parse the revision yourself, you just echo back what the server gave you.

The flow is:

1. `GET` the record. The response carries an `ETag` header — an opaque token
   identifying the record's current revision.
2. `PUT` your change with an `If-Match` header set to that exact `ETag` value
   (quotes included — send it back verbatim).
3. The `PUT` response carries a **new** `ETag` (the revision your change just
   created). Reuse it as the `If-Match` for your next `PUT` — you do **not**
   need to `GET` again between saves.

Responses you must handle:

| Status | Meaning | What to do |
|--------|---------|------------|
| `200 OK` | Saved. Response has a fresh `ETag`. | Use that `ETag` for your next save. |
| `428 Precondition Required` | You sent no `If-Match`. | `GET` the record, then retry with its `ETag`. |
| `409 Conflict` | Someone else changed the record since your `ETag`. Your change was **not** saved; the response body is the document you tried to submit, so nothing is lost. | `GET` the record afresh, re-apply your change on top of the current version, and `PUT` again with the new `ETag`. |

The token is **per record** — a save to one record never causes a conflict on
another. Retrieving history versions (`GET /history/{revision}/{identifier}`)
is unaffected.

Treat the `ETag` as opaque: it is a compound value and its internal structure
may change. Send it back exactly as received. Both the strong (`"…"`) and weak
(`W/"…"`) forms are accepted on `If-Match`.

`If-Match: *` is also accepted, with its standard HTTP meaning — "apply this
to whatever the current version is". It satisfies the precondition without
pinning a revision, so it **disables the lost-update protection** for that
request. Use it only for writes that are genuinely safe to apply blind.

The same mechanism covers the **service agreement** endpoints, which are
edited over long sessions and are protected identically:

    PUT /service-agreement/{identifier}
    PUT /service-agreement/{identifier}/permission

and the permission and catalogue panels of a record:

    PUT /documents/{identifier}/permission
    PUT /documents/{identifier}/catalogue
    PUT /documents/{identifier}/catalogue-view

Reading either permission list (`GET …/permission`) needs the same edit right
as changing it, because the list names the login of every account with access
to the record. Anyone else, including an anonymous caller, gets `403`.

### curl

Capture the `ETag` from the `GET`, then send it back on the `PUT`. `-D -`
dumps response headers so you can see the ETag; `-i` on the PUT shows the new
one.

```bash
BASE=https://catalogue.ceh.ac.uk
TOKEN=glpat-xxxxxxxxxxxxxxxxxxxx     # your personal access token
ID=9c3c...your-record-id

# 1. GET the record and capture its current ETag from the response headers
etag=$(curl -sS -H "Authorization: Bearer $TOKEN" -D - -o record.json \
             -H 'Accept: application/json' \
             "$BASE/documents/$ID" \
        | awk -F': ' 'tolower($1)=="etag"{print $2}' | tr -d '\r')
echo "current revision: $etag"       # e.g. "3f1a9c..."

# ...edit record.json however your script needs to...

# 2. PUT the change back, echoing the ETag verbatim as If-Match.
#    -i so we can read the NEW ETag from the response headers.
curl -sS -i -H "Authorization: Bearer $TOKEN" -X PUT \
     -H 'Content-Type: application/gemini+json' \
     -H "If-Match: $etag" \
     --data @record.json \
     "$BASE/documents/$ID"
# 200 OK  -> response headers include a new  ETag: "..."  (reuse it next time)
# 428     -> you omitted If-Match
# 409     -> the record moved on; GET again, re-apply, retry
```

Authentication uses a **personal access token** sent as a `Bearer` token
(`Authorization: Bearer glpat-…`), not HTTP basic auth.

Note the `If-Match` value is the ETag **with its surrounding quotes** exactly
as the server sent it.

### Python

A small helper that fetches, edits, and saves with the correct headers, and
retries once on a `409` by re-reading and re-applying the edit. It reuses the
`ETag` returned by each `PUT`, so a run of consecutive saves needs only one
initial `GET`.

```python
import requests

BASE = "https://catalogue.ceh.ac.uk"
TOKEN = "glpat-xxxxxxxxxxxxxxxxxxxx"   # your personal access token
MEDIA_TYPE = "application/gemini+json"

# Authenticate with a personal access token as a Bearer token (not basic auth).
# A Session applies the Authorization header to every request.
session = requests.Session()
session.headers["Authorization"] = f"Bearer {TOKEN}"


def get_record(record_id):
    """Return (document_dict, etag) for a record."""
    resp = session.get(
        f"{BASE}/documents/{record_id}",
        headers={"Accept": "application/json"},
    )
    resp.raise_for_status()
    return resp.json(), resp.headers["ETag"]


def save_record(record_id, document, etag):
    """PUT a change guarded by If-Match. Returns (saved_document, new_etag).

    Raises Conflict if the record changed since `etag` was obtained.
    """
    resp = session.put(
        f"{BASE}/documents/{record_id}",
        json=document,
        headers={
            "Content-Type": MEDIA_TYPE,
            "Accept": "application/json",
            "If-Match": etag,          # send the ETag back verbatim
        },
    )
    if resp.status_code == 409:
        raise Conflict(resp.json())    # body is your unsaved submission
    if resp.status_code == 428:
        raise RuntimeError("If-Match header was missing")
    resp.raise_for_status()
    return resp.json(), resp.headers["ETag"]   # reuse new_etag for the next save


class Conflict(Exception):
    def __init__(self, submitted_document):
        super().__init__("record changed since it was read")
        self.submitted_document = submitted_document


def edit(document):
    """Your change to the record goes here."""
    document["title"] = document["title"] + " (revised)"
    return document


def save_with_retry(record_id, apply_edit, attempts=3):
    """Fetch, apply an edit, and save — retrying on a conflict by re-reading
    the current version and re-applying the edit on top of it."""
    document, etag = get_record(record_id)
    for _ in range(attempts):
        try:
            saved, etag = save_record(record_id, apply_edit(document), etag)
            return saved, etag
        except Conflict:
            document, etag = get_record(record_id)   # someone else saved; rebase
    raise RuntimeError(f"gave up after {attempts} conflicting attempts")


if __name__ == "__main__":
    record_id = "9c3c...your-record-id"

    # One-shot edit with automatic conflict retry:
    saved, etag = save_with_retry(record_id, edit)

    # Consecutive saves in one session reuse the returned ETag — no re-GET:
    saved, etag = save_record(record_id, edit(saved), etag)
```