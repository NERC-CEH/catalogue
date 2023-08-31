# eLTER imports
When the `server:elter` profile is active, activating the `imports` profile activates two regular data imports: one for SITES data (https://www.fieldsites.se/en-GB) and one for B2SHARE data (https://b2share.eudat.eu/).

Both imports run as a preset user which are present in any dev environment (DevelopmentUserStoreConfig) but MUST exist in crowd for real deployments.
Crowd passwords are irrelevant so set securely.
Both users must have the ability in crowd to create and publish records!

**WARNING:** this is meant to be a friendly overview of the imports but remember that the code is the ultimate source of truth!

## SITES import
- code is in SITESImportService.java
- runs one minute after startup and every week thereafter
- user is "SITES metadata import"/"info@fieldsites.se"
- fetches https://meta.fieldsites.se/sitemap.xml and iterates through each record:
    - fetches it and parses its ld+json representation
    - creates new records and updates existing ones, matching source records to local ones by handle.net identifier (e.g. `https://hdl.handle.net/11676.1/P8rtv97XQIOXtgQEiEjwokOt`)
    - only imports records of type "Dataset"
- logs start of import and end of import with summary
- therefore does nothing about records which disappear from the source (which does happen)

## B2SHARE import
- code is in B2shareImportService.java
- runs one minute after startup and every week thereafter
- user is "B2SHARE metadata import"/"info@eudat.eu"
- iterates through all records in the lter "community" in B2SHARE via the API (https://b2share.eudat.eu/api/...):
    - creates new records and updates existing ones, matching source records to local ones by DOI (e.g. `10.23728/b2share.b56cd875765a403599859177fced08ae`)
    - records are only published on creation and only if a deims site has been detected
    - skips a hardcoded list of DOIs which were imported separately in a different format
- logs start of import and end of import with summary
- therefore does nothing about records which disappear from the source (which does happen)

### Imported fields
- id
- created
- title(s) (>1 to alt titles)
- descriptions (combined with labels if appropriate)
- creators
- contact_email
- publication_date
- DOI
- open_access
- metadata_url (deims site via regex)
- keywords
- disciplines
