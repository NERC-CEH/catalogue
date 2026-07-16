# API Documentation
A REST API to create, view, modify and delete metadata records.

Replace curly brackets {} with appropriate content.

### Defined Values
[CatalogueServiceConfig](../java/src/main/java/uk/ac/ceh/gateway/catalogue/config/CatalogueServiceConfig.java) for valid values of {catalogue identifier}

[WebConfig](../java/src/main/java/uk/ac/ceh/gateway/catalogue/config/WebConfig.java) for valid values of {document type}

### File Identifier
The jsonpath of {identifier} from the response payload is

    $.id

## Create

    POST /documents?catalogue={catalogue identifier}

    Accept: application/json
    Authorization: Basic {Base64 username:password}
    Content-Type: application/{document type}+json

    {JSON request payload}

## View

    GET /documents/{identifier}

    Accept: application/json
    Authorization: Basic {Base64 username:password}

## Modify

    PUT /documents/{identifier}

    Accept: application/json
    Authorization: Basic {Base64 username:password}
    Content-Type: application/{document type}+json
    If-Match: "{revision}"

    {JSON request payload}

The `If-Match` header is **required** on every modify request — see
[Concurrency control](#concurrency-control-optimistic-locking) below. A
request without it is rejected with `428 Precondition Required`; a request
carrying a revision that is no longer current is rejected with `409
Conflict`.

## Delete

    DELETE /documents/{identifier}
    Authorization: Basic {Base64 username:password}

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