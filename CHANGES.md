# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [3.209.0] - 2026-06-17

This release makes catalogues discoverable by external data tools and search engines: each catalogue now publishes a machine-readable description of its contents, SPARQL endpoint, and vocabulary following the W3C VoID standard, and exposes this via OpenAPI documentation and HTML link headers (DRI-ONE-91). A fix corrects the MapServer connection path so geospatial map layers work correctly with the updated routing configuration.

Search filters for resource type and resource status have been corrected so results match what users expect from the legacy search interface — the record type and availability facets now translate values properly (EMC-869). A routine properties update has also been applied. Behind the scenes, the server infrastructure has been updated to Node.js 24 LTS with the latest container base images, and security patches have been applied.

## [3.208.0] - 2026-06-10

Grant and funding information is now included in the structured metadata published with each record, making funding provenance visible to search engines and data aggregators. Schema.org metadata has been moved to the HTML page head for better search engine compatibility, and the Open Government Licence reference has been corrected (DRI-ONE-72). The in-licensed catalogue, which was no longer in use, has been removed (EMC-861). MapServer has been upgraded to Camptocamp 8.0, which is required for the current map hosting infrastructure (EMC-394), and a project vocabulary has been added for keyword lookups (EMC-864).

## [3.207.0] - 2026-06-02

A maintenance announcement banner can now be displayed across the catalogue during planned upgrade windows. The default logging level in production has been set to warnings-only, significantly reducing log volume. The display of Statistical Variable records has been updated to use the PropertyValue format as required (EMC-593).

## [3.206.0] - 2026-05-19

GeoPackage file type is now supported in the map layer editor, enabling datasets published in this modern geospatial format to be configured correctly (EMC-853). A bug preventing colour styles from being saved in the catalogue editor has been fixed (EMC-840). The production server now runs as a non-root user, improving security. A sticky header flickering issue on mouse-wheel scroll has also been resolved.

## [3.205.0] - 2026-05-13

The OpenAPI documentation server URL is now derived from configuration rather than hard-coded, ensuring it is correct across all environments (EMC-852). RDF/Turtle identifier handling has been corrected, and a new endpoint allows catalogues to be exported to the linked data store on demand (EMC-842). Unhelpful "not found" and "permission denied" errors have been downgraded to warnings in the server logs to reduce alert noise.

## [3.204.0] - 2026-05-11

The search index has been extended with new field types to support more advanced querying (EMC-844). The linked data export has been improved and now logs more informative error messages when indexing fails (EMC-842, EMC-850). Error and diagnostic log messages have been made more descriptive across several services (EMC-846). A typo in a sort option label ("citatons" → "citations") has also been corrected.

## [3.203.0] - 2026-05-08

Search facet filters are now validated on input, returning a clear error rather than silently ignoring unrecognised filter values (EMC-845).

## [3.202.0] - 2026-05-07

This release delivers a significant infrastructure upgrade: Solr has been upgraded to version 10 (EMC-830) and Java to version 25. Full OpenAPI/Swagger documentation is now published for the REST API, making it easier for developers to integrate with the catalogue (EMC-834). Search has been improved with better tokenisation for partial-word matching. A timeout affecting the checksum CSV download for large datasets has been fixed by switching to paginated streaming (EMC-814). Several bugs have also been addressed, including a null pointer error in catalogue retrieval, a template rendering error (EMC-831), and a file identifier mismatch in machine-readable exports (EMC-789).

## [3.201.0] - 2026-04-23

Users can now search across all catalogues simultaneously using a new "all catalogues" option in the search interface. A data format field has been added to the search index so records can be filtered by file type (EMC-823). RDF metadata predicates for monitoring classes have been updated to align with Dublin Core and other international standards (EMC-806).

## [3.200.2] - 2026-04-14

A units-of-measure field has been added to the spatial resolution property in record editors, making data precision clearer to users (EMC-810). A download error affecting certain records has been fixed (EMC-816), along with a Turtle/RDF serialisation bug (EMC-792) and a schema error in some metadata records (EMC-800).

## [3.200.0] - 2026-03-19

Internal RDF relationship identifiers have been replaced with standard Dublin Core equivalents, improving interoperability with external linked data systems (EMC-802). Downloads are now supported for records from non-EIDC catalogues, extending this capability beyond the EIDC dataset collection.

## [3.199.0] - 2026-03-09

Users can now choose from multiple simultaneous download options for a dataset (EMC-798), and a new data availability field makes it clear when and how data can be accessed (EMC-785). The data previewer button now appears for dataset collections as well as individual datasets (EMC-777). Metadata dates are now included in the search index, improving sort and filter accuracy (EMC-790). Research Organisation Registry (ROR) identifier links are resolved correctly in the production environment (EMC-781). Several layout and display fixes have also been applied (EMC-778), along with a Croissant schema correction (EMC-796) and a fileset error fix (EMC-801).

## [3.198.0] - 2026-01-16

This is a substantial release. The EIDC brand has been refreshed with updated logos and styles (EMC-754), and old legacy branding has been removed (EMC-775). Users can now search for datasets by proximity to a location using a configurable buffer radius (EMC-733), and dataset collections now display a data preview button (EMC-762). A new endpoint provides a stable resource identifier for each record (EMC-744). The Croissant machine-readable metadata export now handles datasets with large numbers of files by grouping them intelligently rather than listing every file individually (EMC-756). The descriptive keywords field has been removed from records (EMC-759). Several fixes address small map polygons not appearing on page load (EMC-734) and a bug in map-viewable detection for certain records (EMC-764).

## [3.197.0] - 2025-11-26

Minor user interface improvements to the catalogue display (EMC-751).

## [3.196.0] - 2025-11-19

Production containers now run as a non-root user, improving the security posture of the deployed application (EMC-426). A bug that prevented data from being posted to the linked data store has been fixed (EMC-743). Two issues in the Croissant machine-readable metadata export have been corrected: whitespace in record identifiers and trailing commas in the schema output (EMC-741, EMC-742).

## [3.195.0] - 2025-11-12

Several user-facing improvements ship in this release. Data previewer links are now embedded directly in dataset pages, so users can preview tabular data without leaving the catalogue (EMC-692). A new search filter lets users find records changed within a specified time period (EMC-719), and a new Application record type has been added alongside existing dataset types (EMC-728). Superseded records are now excluded from public search results to reduce clutter (EMC-727). The Croissant machine-readable export has been improved for better schema conformity (EMC-726), and licence URLs have been updated to current addresses (EMC-738).

## [3.194.0] - 2025-10-14

Access to citizen science datasets has been improved: a new endpoint retrieves file information directly from the data storage service, making it easier to browse and download files associated with these datasets (EMC-716).

## [3.193.0] - 2025-10-13

Amazon Cognito is now supported as an authentication option, enabling organisations to log in to the catalogue using their existing cloud identity accounts (EMC-624). Search results layout and interface transitions have also been improved (EMC-715).

## [3.192.0] - 2025-10-09

Behind-the-scenes maintenance: server log verbosity has been reduced to avoid filling log storage with routine operational noise (EMC-714).

## [3.191.0] - 2025-10-06

The file upload page has been improved to handle larger numbers of files, with better sorting by status and file name and configurable display limits (EMC-704). A bug causing zip uploads created on macOS to fail — due to hidden system files automatically included by macOS — has been fixed (EMC-707).

## [3.190.0] - 2025-09-25

Two new features in this release: a site-wide announcement banner can now be displayed across the catalogue to communicate maintenance windows or important notices (EMC-705), and citation metadata is now automatically harvested from GitHub repositories that publish a CITATION.cff file, making software datasets easier to cite correctly (EMC-595).

## [3.189.0] - 2025-09-17

Metadata records can now include contributor roles, allowing data depositors to credit the full range of people involved in creating a dataset (EMC-697). A bug affecting the DataCite service — which supplies metadata to the international DOI registry — has been fixed to prevent errors when records have incomplete information (EMC-680).
