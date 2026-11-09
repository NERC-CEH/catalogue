# Metrics migration: SQLite to PostgreSQL

One-off migration of the legacy SQLite metrics (`views`, `downloads`) into the PostgreSQL
`metrics.documents` and `metrics.metric_counts` tables.

## 0. Timing and extraction

**Take the copy after the catalogue is running on postgres and the old sqlite pod has terminated.**

If you copy before the switch, every count recorded between the copy and the switch is lost for good.

Extract the SQLite database from the catalogue pod (this only works while the SMB share is still mounted):

```bash
kubectl -n eidc exec deploy/catalogue -- cat /var/ceh-catalogue/metrics-db/metrics.db > metrics-copy.db
```

## 1. SQLite: export

Copy the SQLite `metrics.db` and `metrics.old.db` to your local machine (work on a copy, not the live file) and run:

```bash
./01_export_sqlite.sh ./metrics-copy.db . ./metrics.old.db
```

This will insert any rows that exist in `metrics.old.db` but not in `metrics-copy.db` for both `views` and `downloads` tables.

If you don't have an older copy of the database with missing rows, just run:

```bash
./01_export_sqlite.sh ./metrics-copy.db .
```

Record the output. Example:

```
views: 1429 rows, sum(amount)=1429
downloads: 71 rows, sum(amount)=71
```

Check that these files were created:

```
views.csv
downloads.csv
```

## 2. PostgreSQL: load

Copy the files to the Postgres pod.

```bash
kubectl -n eidc exec catalogue-db-0 -- mkdir -p /tmp/migration
kubectl -n eidc cp 02_load_postgres.sql  catalogue-db-0:/tmp/migration/02_load_postgres.sql
kubectl -n eidc cp views.csv      catalogue-db-0:/tmp/migration/views.csv
kubectl -n eidc cp downloads.csv  catalogue-db-0:/tmp/migration/downloads.csv
```

Log in to Postgres:

```bash
kubectl -n eidc exec -it catalogue-db-0 -- psql -U catalogue -d catalogue
```

Run the migration script:

```
\cd /tmp/migration
\i 02_load_postgres.sql
```

## 3. Verify the result

Example output:

```
BEGIN
CREATE TABLE
CREATE TABLE
ALTER TABLE
COPY 1429             -- rows read from views.csv
INSERT 0 1429         -- views staged
TRUNCATE TABLE
COPY 71               -- rows read from downloads.csv
INSERT 0 71           -- downloads staged
DO                    -- duplicate check passed (not migrated before)
                      -- (empty) no rows skipped for a non-UUID document
DELETE 0              -- no rows removed
INSERT 0 158          -- documents inserted or updated in metrics.documents
INSERT 0 1500         -- counts inserted into metrics.metric_counts
 view     | 1429 | 1429   -- rows and total amount from SQLite (staged)
 download |   71 |   71

 download |   71 |   71   -- rows and total amount migrated into Postgres
 view     | 1429 | 1429

COMMIT
ANALYZE
ANALYZE
```

Check:

- The `COPY` numbers match the export output from step 1.
- The SQLite and Postgres totals are identical for both `view` and `download`.
- `COMMIT` is shown. If it isn't, nothing was saved: run `ROLLBACK;`, fix the error, and run `\i 02_load_postgres.sql` again.

The `0` in `INSERT 0 n` is a legacy OID field and is always 0. `n` is the row count.

## 4. Clean up

```
\! rm -rf /tmp/migration
\q
```

Keep the original `metrics.db` until the SQLite rollback path is removed. Do not commit `export/*.csv` or `*.db` to the repository.

## Notes

- The script runs in a single transaction: on any error, PostgreSQL is left unchanged and the script is safe to rerun.
- Rerunning after a successful migration stops with `legacy rows already present ... already migrated?`, so data can't be loaded twice.
- Counts the app has already written to Postgres are kept; existing document titles and record types are not overwritten.
- Record pages may take up to 10 minutes (cache expiry) to show the migrated totals.
