# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [3.228.0] - 2026-08-20

Several long-standing annoyances in the record editor have been fixed. Adding a row to a list field such as keywords no longer writes an empty entry into the record before anything has been typed; those blank entries were being saved and then appeared on the published record as a stray comma in front of the first real keyword, or as a labelled row with no value at all (GH dri-one#297). Records that already carry such an entry now render correctly without needing to be re-edited.

Deleting a row from the middle of a list no longer corrupts the rows beneath it. The remaining rows were left out of step with the stored values, so a later edit could write into the wrong position — the form would show two entries while the record saved three, keeping the pre-edit value alongside the new one (GH dri-one#298). Six fields were affected, among them alternative titles and model inputs and outputs. An unused editor component has also been removed (GH dri-one#302).

## [3.227.0] - 2026-08-18

Search gains new ways to narrow results. The FDRI filters are now available on the all-catalogue search interface, and the UKCEH catalogue has an environmental domain filter (GH dri-one#149). Grouping terms that exist only to organise those filters are no longer offered when an editor picks keywords, so they cannot be attached to a record by mistake. Results can also now be filtered by keyword directly from the web address, which makes it possible to share a link to a pre-filtered set of results (DRI-ONE-276).

Monitoring Network records were failing with an error as soon as a monitoring site carrying a resource identifier was added to them; the outlines for those networks are now assembled correctly and the records display again (GH dri-one#279). The map service has been protected against oversized image requests, which had been exhausting its memory and forcing it to restart — fourteen times in production (GH dri-one#288). Smaller touches: pages now show a site icon in the browser tab, web addresses ending in a slash redirect to the correct page, and responses tell caches that the same address can return more than one format (GH dri-one#292, GH dri-one#293).

## [3.226.0] - 2026-08-17

The search index now recovers on its own when the search service is unavailable at start-up. A release can restart the catalogue before the search service is ready, and when that happened the index was left empty with nothing written to the logs, so the catalogue appeared to hold no records until somebody noticed and rebuilt it by hand — production sat in that state for fourteen minutes (GH dri-one#283). The application now retries until it can confirm the state of the index, and reports clearly what it found and what it rebuilt. Changelog entries for releases 3.222.0 to 3.225.0 were also added.

## [3.225.0] - 2026-08-17

Staff can once again save a record immediately after creating it. A record created and then edited on without leaving the editor was being rejected with an error, and the only way past it was to leave the editor without saving and go back in — which risked losing the work in progress. The same error affected link records on their very first save. Both now save normally (GH dri-one#282). No data was ever lost to this: the failed saves were refused rather than applied incorrectly.

## [3.224.0] - 2026-08-13

Depositors can now record the science area their submission belongs to on the deposit request form, and the confirmation page shown after a successful submission is now reliable — the reference number is carried in the web address rather than held in the browser session, which previously meant the confirmation could come up blank (DRI-ONE-267). Supporting this, the application no longer keeps per-visitor state on the server, so it behaves consistently when more than one copy is running behind the load balancer (DRI-ONE-271).

When linking one record to another, the search suggestions are now limited to record types that are valid for the relationship being created, and a record no longer appears in its own list of suggestions (EMC-893). The relationship editor's appearance has also been tidied.

## [3.223.0] - 2026-08-06

Deleting a record now also removes its accumulated view and download statistics, so figures for a deleted record no longer linger in usage reporting. Every deletion is written to an audit log recording who removed what and when. Behind the scenes, a gap in the build was closed so that changes to page templates are properly picked up by the automated tests, and changelog entries for releases 3.219.0 to 3.221.0 were added.

## [3.222.0] - 2026-08-05

Administrators holding the record-deletion role can now remove any record in the catalogue, including records whose stored content the application can no longer read and which previously had to be left in place. The deletion facility was reviewed before release and several issues were corrected: the protection that stops another website triggering actions on a signed-in user's behalf was not being applied on the alternative sign-in routes, record identifiers were not being safely escaped when displayed, a failure during deletion could pass unreported, and a sign-in cookie was being cleared when it should not have been. Deletion is now also restricted to the web interface rather than being callable as a general service.

## [3.221.0] - 2026-08-04

Record pages load substantially faster. Their view and download totals were being recalculated on every page view by scanning the entire usage-statistics database, which had grown past a gigabyte and is held on network storage; those totals are now looked up through an index and cached, taking the work off the page-rendering path entirely. A leftover template belonging to a record type withdrawn some time ago has also been removed.

## [3.220.0] - 2026-08-03

The metadata editor now prevents two people's changes from overwriting one another. Previously, if two editors opened the same record and saved in turn, the second save silently discarded the first person's work with no warning to either of them. The editor now recognises that the record has changed since it was opened, says so, and keeps the unsaved edits on screen so they can be reapplied rather than retyped. The same protection covers the catalogue, catalogue view and permission editors, and service agreements, and the public API gained matching support with worked examples in the documentation.

Records whose stored content the application can no longer read — left behind when a record type is withdrawn — can now be deleted instead of remaining stuck in place. Saving is also markedly quicker, because checking whether a record has changed no longer means working back through its entire revision history. This release additionally carries the two fixes previously issued as urgent patches (3.219.1 and 3.219.2), restores the access button on records that are only viewable as a map, corrects line styling on map layers, and includes the record identifier in conflict messages written to the logs so they can be traced to the record concerned.

## [3.219.2] - 2026-08-03

Urgent patch. Record pages were failing with an error whenever the usage-statistics database was locked by another process. Pages now render normally regardless of what else is using that database.

## [3.219.1] - 2026-08-03

Urgent patch. Records that are viewable as a map were returning a server error instead of displaying at all. Those records now open as expected.

## [3.219.0] - 2026-07-30

Datasets that are only available offline — supplied on request rather than downloaded directly — are now handled correctly when a user places an order for them (DRI-ONE-221). Checks against the list of permitted contact addresses are now case-insensitive, so records whose contact email was recorded with different capitalisation are no longer wrongly reported as invalid.

## [3.218.0] - 2026-07-30

A bug that caused manual changes to a record's resource type to silently revert after saving has been fixed (GH dri-one#214); a similar crash risk in citation-link rendering was also guarded against. The sitemap.xml file published for search engines has been corrected to meet XML formatting requirements and now includes a last-modified date for each entry (DRI-ONE-68). ORCID researcher identifiers are now indexed as exact values rather than being split into separate searchable words. Behind the scenes, cache performance statistics are now exposed via the monitoring endpoint, a misplaced configuration file was moved to its correct location, Docker image publishing was fixed to push only the tag just built rather than every tag in the local image store, and the underlying container tooling was upgraded.

## [3.217.0] - 2026-07-27

This release fixes a server error (HTTP 500) that occurred when viewing signpost records with no distributor contact specified, restoring normal page rendering for those records.

## [3.216.0] - 2026-07-24

Contact and distributor information on records has been substantially reworked: publisher, author, and distributor roles are now handled by a dedicated data structure rather than a single combined field, fixing several gaps including missing DataCite contributors, an omitted role in Citation File Format exports, and duplicate entries in role-based search filters (EMC-700). The record editor gained two small but useful additions — Parquet is now available as a predefined file format, and Principal Investigator has been added to the list of contact roles — and a bug causing the download button to appear on records with no actual download has been fixed. WMS map layers can now be styled with custom line thickness that increases as users zoom in, giving line-based geospatial layers a clearer appearance. The EnvThes vocabulary is now available for keyword selection, HTTPS redirects have been corrected to work with local developer setups, and dataset-monitoring relationship data has been moved into the core monitoring metadata model for consistency (EMC-892).

## [3.215.0] - 2026-07-13

This release improves the reliability of the underlying storage system: the scheduled clean-up of the Git-backed datastore can now be disabled or rescheduled to suit operational needs, permission checks have been sped up by routing them through the existing cache, and the whole caching layer has been migrated to a faster, more modern library. The record editor no longer shows a premature "Saved!" confirmation before the server has actually confirmed the save, and the user account menu in the navigation bar has been rebuilt to render natively, giving it a more consistent, modern look. Search results now correctly round map bounding-box coordinates and no longer carry redundant parameters in the page URL; dataset relationship information has also been relocated into the core metadata model for consistency (EMC-889). Behind the scenes, a major internal library upgrade (ukceh 2.0.0) was completed, noisy log output during search-index maintenance was fixed, and automated test coverage for the web interface was substantially expanded.

## [3.214.0] - 2026-07-01

This release delivers a noticeable speed-up to how individual record and dataset pages are generated. The system now prepares its internal data queries once and reuses them, instead of rebuilding them from scratch on every page view, and it keeps recently viewed records in fast memory for longer. Together these changes roughly halve the time taken to display a record page and reduce the load placed on the underlying storage system.

## [3.213.0] - 2026-07-01

JIRA integration has been restored to using personal access tokens for authentication, following a brief rollback to the older password-based method while a line-ending issue in the token was tracked down and fixed.

## [3.212.0] - 2026-06-30

JIRA integration was temporarily reverted to basic username/password authentication after the personal access token approach introduced in the previous release caused problems; this was a short-lived stopgap ahead of the fix in the next release.

## [3.211.0] - 2026-06-30

Records can now appear in more than one catalogue at once, giving publishers more flexibility in how datasets are organised and shared (EMC-112). When JIRA is unavailable, the upload page no longer fails with a server error — a branded, status-aware error page is now shown instead, and JIRA authentication has moved to a personal access token (EMC-881). Metadata records now load noticeably faster: individual record reads are cached to cut down on file-system round trips, and several related indexing bugs have been fixed, including stale search results after saving and a data type mismatch in the caching layer. The local development environment has also been improved, running containers as the host user and self-healing file permission issues automatically.

## [3.210.0] - 2026-06-24

Schema.org credit information is now published as a proper citation rather than plain text (DRI-ONE-98). Access limitations on records have been updated to reference the latest NERC vocabulary term (EMC-872), and a false-positive error that could block saving a record with an existing resource identifier has been fixed. Depositor permissions in service agreements are now handled consistently regardless of letter case (EMC-862). A change log has also been introduced to track releases going forward.

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
