-- The metrics schema for the SQLite rollback path (metrics.database.engine=sqlite). See #234/#235.
--
-- SQLite stays selectable until the PostgreSQL migration (#236) is verified, and the PostgreSQL scripts
-- cannot be applied to it, so it has its own location. Applied by MetricsSchemaMigrator like the
-- PostgreSQL one; this directory, the sqlite branch of MetricsDatabaseConfig and sqlite-jdbc all go when
-- the rollback window closes.
--
-- Two cases:
--  * the existing production file: it already has both tables and #242's covering indexes and no
--    flyway_schema_history, so MetricsSchemaMigrator baselines it at version 1 and this script is
--    SKIPPED -- nothing here runs against it. Its tables keep their shape (no id column), which is fine:
--    inserts name their columns.
--  * a fresh local/CI file: empty, so it is not baselined and this script creates the schema, with the
--    same shape as PostgreSQL's.
-- IF NOT EXISTS is kept anyway, as a guard against a partially created file.
--
-- Deliberately NOT the report indexes PostgreSQL gets: the rollback path keeps exactly the indexes
-- production has today.

CREATE TABLE IF NOT EXISTS views (
    id              INTEGER PRIMARY KEY,
    start_timestamp bigint  NOT NULL,
    end_timestamp   bigint  NOT NULL,
    amount          integer NOT NULL,
    document        text    NOT NULL,
    doc_title       text    NOT NULL,
    record_type     text    NOT NULL
);

CREATE TABLE IF NOT EXISTS downloads (
    id              INTEGER PRIMARY KEY,
    start_timestamp bigint  NOT NULL,
    end_timestamp   bigint  NOT NULL,
    amount          integer NOT NULL,
    document        text    NOT NULL,
    doc_title       text    NOT NULL,
    record_type     text    NOT NULL
);

-- #242's covering indexes, same names and shape as the ones already on the production file.
CREATE INDEX IF NOT EXISTS idx_views_document_amount     ON views     (document, amount);
CREATE INDEX IF NOT EXISTS idx_downloads_document_amount ON downloads (document, amount);
