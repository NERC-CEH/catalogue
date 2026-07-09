# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Testing
- Run the test suite scoped to what changed, especially after upgrades, refactors, or migrations:
  - Java changes → `./gradlew :java:test`
  - Frontend-only changes (`web/`) → `cd web && npm run test` and `npm run standard`; the Java suite is not required
  - Mixed changes → run both
- When fixing one test failure, check for similar patterns elsewhere (e.g., if one method needs a transaction wrapper, audit all similar methods)
- Verify null-safety fixes cover both missing keys AND keys with null values

## Security
- When implementing multi-tenant or cross-catalogue endpoints, always verify authorization filters apply to ALL returned records, including unpublished/draft states
- Explicitly check publisher/role boundaries before returning data across catalogues

## Build & Tooling
- This project uses Java 25 with Gradle; prefer toolchain configuration over sourceCompatibility/targetCompatibility
- Library versions live in `gradle/libs.versions.toml` (Gradle version catalog) — add/update versions there, not inline in `build.gradle`
- Dockerfile multi-stage build: copy `gradle/libs.versions.toml` into the Gradle build stage alongside `build.gradle`, or `bootJar` will fail with missing catalog
- Any Gradle invocation resolves the `java-commons` library (`uk.ac.ceh.components:*`) from a private GitLab Maven registry (`java/build.gradle`), authenticated one of three ways depending on context:
  - **Host/IntelliJ** (`./gradlew :java:compileJava`, `:java:test`, project import, etc.): a `gitlabToken` project property, sent as a `Private-Token` header. Needs a GitLab personal access token with `read_api` scope, set in `~/.gradle/gradle.properties` (not project-local, so it never gets committed):
    ```properties
    gitlabToken=<personal access token with read_api scope>
    ```
  - **Docker Compose** (`docker compose up`/`--build`/`watch`, the `dev-run` target): the `catalogue` service's Gradle home is the `gradle-cache` named volume, not the host's `~/.gradle` — it does not inherit the host property above. Needs `ORG_GRADLE_PROJECT_gitlabToken=<same personal access token>` in `secrets.env` (gitignored, loaded via `env_file` in `docker-compose.yml`) — Gradle auto-maps `ORG_GRADLE_PROJECT_*` env vars to project properties, so no code changes are needed.
  - **CI** (`.gitlab-ci.yml` `test_java` job, and the Dockerfile's `build-java` stage via a BuildKit secret): the auto-injected `CI_JOB_TOKEN`, sent as a `Job-Token` header — no CI/CD variable needed on this project's side. `commons/java-commons`' job-token scope allowlists the `eip` group (not `eip/catalogue` individually), which covers this project since it belongs directly to that group
  - Without one of these, Gradle fails resolving `java-commons` dependencies with a `401 Unauthorized`

## Commands

### Java (Gradle)
```bash
./gradlew :java:build          # build
./gradlew :java:test           # all tests
./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.search.SearchControllerTest        # single class
./gradlew :java:test --tests uk.ac.ceh.gateway.catalogue.search.SearchControllerTest.myTest # single method
./gradlew :java:compileJava    # compile only
```

### JavaScript (web/)
```bash
cd web && npm install
npm run build-dev && npm run build-css-dev  # dev build
npm run standard                             # lint (StandardJS)
npm run test                                 # Karma/Jasmine tests (single run)
npm run test-server                          # tests in watch mode
npm run watch                                # rebuild JS on changes
npm run watch-css                            # rebuild CSS on changes
```

### Run the full application

The primary development workflow uses Docker Compose — a single command starts all services
including the Java application and webpack watcher:

```bash
# First run, or after changing the Dockerfile or entrypoint script:
docker compose up --build

# Subsequent runs (reuses the built image and cached Gradle dependencies — much faster):
docker compose up

# Rebuild only the catalogue image (e.g. after changing the Dockerfile):
docker compose build catalogue && docker compose up
```

The application is available at http://localhost:8080.

**Hot-reload while running:**

| What changed | Action required | Delay |
|---|---|---|
| `.java` source | `./gradlew :java:compileJava` (on host) | ~5–15s |
| `templates/` | Refresh browser (Freemarker cache off) | instant |
| `web/src/` JS | webpack watch rebuilds automatically | ~3–10s |
| `web/scss/` CSS | gulp watch rebuilds automatically | ~2–5s |
| `nginx.conf` | `docker compose restart nginx` | ~1s |
| `build.gradle` / `libs.versions.toml` | automatic with `docker compose watch` | rebuild |
| `web/package.json` / `package-lock.json` | `docker compose restart web` | ~rebuild |

Use `docker compose watch` instead of `docker compose up` to enable automatic rebuilds when
Dockerfiles, Gradle build files, or the entrypoint script change.

**Optional services** use Docker Compose profiles:

```bash
docker compose --profile hubbub up --build   # include Hubbub upload service
docker compose --profile legilo up --build   # include Legilo
docker compose --profile fuseki  up --build  # include Fuseki SPARQL
```

## Architecture

This is a **multi-catalogue metadata management system** for environmental/scientific datasets (UKCEH). A single Spring Boot application serves multiple catalogues (EIDC, ASSIST, etc.), each with their own document types and permission rules.

### Backend (Java/Spring Boot 4.0.5)

**Layers:**
- **Controllers** → **Services** → **Repository** (strict separation; services are tested independently of HTTP)
- **Freemarker templates** (`templates/`) generate all output formats: HTML, XML, JSON-LD, Turtle, citations

**Storage:** Metadata documents are persisted in a **Git repository** (`datastore/`) via the CEH Components datastore library. Every save is a Git commit. This is not a database — it's a Git-backed document store.

**Search:** Metadata is indexed into **Solr** for faceted full-text search, and into **Apache Jena** for RDF/SPARQL and linked data endpoints. Both indexes are rebuilt from the Git store. Any change to `solr/documents/conf/managed-schema` requires a full Solr reindex to take effect.

**Document types** all extend `AbstractMetadataDocument`. Key types:
- `GeminiDocument` — INSPIRE/ISO 19115 metadata (most common)
- `CEHModel`, `CEHModelApplication` — software/model records
- `MonitoringFacility`, `MonitoringNetwork` — EF Monitoring
- Many others in `sa/`, `modelnerc/`, `upload/` packages

**Adding a new document type** requires changes in 4 places (per README): the model class, a Freemarker template, catalogue config, and the Solr indexer.

**Spring profiles** control optional features:
- `hubbub` / `legilo` — file upload backends
- `fuseki` — SPARQL/RDF support
- `basic-search` / `enhanced-search` — search variants
- `service-agreement`, `metrics`, etc.

**Authentication** uses a `Remote-User` HTTP header (set by the nginx reverse proxy). In development, a "Dev Bar" in the UI allows masquerading as any user.

### Frontend (JavaScript/Backbone + Webpack)

All JS source lives in `web/src/`. Each subdirectory is a **self-contained Backbone.js micro-app** (Models, Views, Routers) bundled separately by Webpack:
- `search/` — faceted search UI
- `editor/` — metadata record editor (different editor views per document type)
- `catalogue/` — browse/view records
- `permission/` — user permission management
- `hubbub/`, `simple-upload/` — file upload
- `study-area/` — geographic/map-based search

JavaScript tests are in `web/src/*/test/` and run with Karma + Jasmine.

CSS is compiled from LESS source via `npm run build-css*`.

### Container stack
- **nginx** — reverse proxy, sets `Remote-User` header
- **catalogue** — Spring Boot application (`dev-run` image; bind-mounts project source for live editing)
- **web** — webpack + gulp watchers for live JS/CSS rebuilds
- **Solr** — search index
- **MapServer** — WMS for geospatial layers
- **PostgreSQL** — only needed with Hubbub profile

### Key libraries
- **Server:** Spring Boot 4.0.5, Spring Security, Freemarker, Apache Solr 9 (SolrJ), Apache Jena 5, Lombok, Jackson, Hibernate Validator, EHCache, CEH Components (Git datastore + Crowd auth)
- **Client:** Backbone.js, jQuery 3, Bootstrap 5, Leaflet 1.9, Select2, DataTables, SweetAlert2

## IntelliJ setup

Enable **Lombok plugin** and turn on **annotation processing** (`Settings > Build > Compiler > Annotation Processors`). Without this, the project will not compile in the IDE.
