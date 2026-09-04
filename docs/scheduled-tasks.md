# Scheduled tasks: threads, shutdown, and what a restart costs

Thirteen `@Scheduled` methods across twelve beans run in this application, and until
dri-one #354 all of them shared **one** thread and were **interrupted** at shutdown. Both
were Spring Boot defaults that nothing had ever set. This page records what runs, what the
scheduling settings in `application.properties` are now set to and why, and — the part a
configuration file cannot state — what none of them covers.

## What runs on a schedule

`@EnableScheduling` on `CatalogueApplication` puts all of these on the auto-configured
`taskScheduler`. Cadences are `fixedDelay`, so a run is scheduled *after* the previous one
finishes and runs cannot overlap or pile up; only the cron entries are wall-clock.

| Bean | Cadence | Profile | What an interrupted run costs |
|---|---|---|---|
| `SolrScheduledReindexService` | 30 min (+ startup retry chain) | all but `test` | A partial Solr index that nothing repairs — dri-one #355 |
| `JenaScheduledReindexService` | 30 min | all | The same, for the RDF store |
| `MapServerScheduledReindexService` | 30 min | all | Mapfiles missing for some records until the next run |
| `JDBCMetricsService.syncDB` | 1 hour | `metrics` | Up to 59 minutes of view/download counts, silently — dri-one #245 |
| `JDBCMetricsService.updateDB` | cron 01:00 | `metrics` | Titles and record types left stale for a day |
| `DataRepositoryOptimizingService` | cron, midnight | all | **Worst case.** An interrupted `git gc` over the CIFS-mounted datastore left an `.idx.new` and took the whole catalogue down. Disabled in prod and staging (`DATA_REPOSITORY_OPTIMIZE_CRON: "-"`), so latent rather than live |
| `FusekiExportService.runExport` | 1 day | `exports` | Only that run's fetches. Each graph is published with a single all-or-nothing PUT, so the previously published graph survives intact — safe by construction, and worth not "fixing" |
| `CatalogueToTurtleService.fetchCatalogues` | 1 day | all | A stale prefetched Turtle payload until the next run |
| `GeminiWafService.fetchFiles` | 1 day | all | A stale WAF file listing until the next run |
| `OrganisationUpdater` | 7 days | all | A partly-downloaded ROR data dump; guarded by a URL recorded in `config.properties` |
| `LocalKeywordVocabulary.retrieve` | 7 days | all | **A week without that vocabulary.** It `deleteByQuery`s the vocabulary from Solr *then* re-adds the terms; interrupted between the two, the keywords are gone until the next weekly run |
| `SparqlKeywordVocabulary.retrieve` | 7 days | all | The same delete-then-add shape, over an HTTP fetch that is genuinely interruptible |
| `GemetVocabularyUpdater` | 7 days | all | A truncated `gemet.json` on the vocabs volume, which is worse than a missing one |

`SolrScheduledReindexService` also schedules its own startup retries — eight attempts
backing off to four minutes — on the same shared scheduler, so before the pool grew those
queued behind everything else too.

## Why the pool is four

`spring.task.scheduling.pool.size` defaults to **1**. Because everything is `fixedDelay`
the symptom was never overlap; it was queueing, and what queues behind what is not
symmetric:

- The three 30-minute reindexers are nearly free when their index is populated:
  `AbstractIndexingService.attemptIndexing` checks `isIndexEmpty()` and otherwise logs at
  debug. But when an index *is* empty that task **is** the repair mechanism — the dri-one
  #283 scenario — and that is exactly when it must not be waiting.
- The Fuseki export became expensive in dri-one #350. On a run that starts with a cold
  description cache it makes roughly 1,660 sequential outbound requests (ORCID 500, DOIs
  300, GtR 259, ROR 200, GeoNames 200, SKOS ~130, DEIMS 61, Wikidata 8 batched), each with
  a 30-second read timeout. Steady state is far smaller — only entities past their refresh
  window — but any restart that loses the cache snapshot produces a cold fill, and that run
  held the single thread for minutes, or hours if an authority was timing out rather than
  refusing.

Four threads decouple the export from the repair paths. Nothing scheduled here is
CPU-bound, so the normally-idle threads cost a stack each, and `fixedDelay` still prevents
a task overlapping *itself*.

## What happens now when the pod stops

`spring.task.scheduling.shutdown.await-termination` defaults to **false** with no period,
which means `ExecutorConfigurationSupport.shutdown()` calls `shutdownNow()`: the running
task is interrupted at an arbitrary point, the queue is discarded, and nothing waits. With
`await-termination=true` and a period of `20s` it calls `shutdown()` instead — no
interruption — and then blocks on `awaitTermination` for up to the period.

Three things about that are easy to get wrong:

- **Graceful web-server shutdown does not do this.** `server.shutdown` holds open in-flight
  HTTP requests and says nothing about scheduled-task state. Note that Boot 4 changed its
  default from `immediate` to `graceful` (`ServerProperties:107`), so this application
  already logs `Commencing graceful shutdown` without anyone setting it — and that log line
  is not evidence that scheduled tasks are being waited for.
- **The period is a total, not a per-task budget.** `awaitTermination` waits for the whole
  pool, so with four threads the 20 seconds covers every task still running, not each.
- **It is also sequential with the lifecycle phase, not concurrent with it.** Setting
  `await-termination` makes the executor skip the coordinated lifecycle stop and do its
  waiting in its destruction step instead, which runs *after* the stop phase. That phase has
  its own budget, `spring.lifecycle.timeout-per-shutdown-phase`, defaulting to 30 seconds —
  so a shutdown held up by a slow in-flight HTTP request could in the worst case spend 30
  seconds there and only then start the 20-second task await, overshooting the 30-second
  grace period and being SIGKILLed. In practice the stop phase completes in milliseconds
  when nothing is in flight (measured below), so this is a tail risk rather than the normal
  case; capping `timeout-per-shutdown-phase` so the two budgets together fit inside the
  grace period is the obvious follow-up, and is deliberately not folded in here because it
  changes request handling.

### Why the queue is dropped rather than awaited

`ScheduledThreadPoolExecutor` cancels *periodic* tasks on `shutdown()` but keeps *delayed
one-shot* ones by default, and it does not terminate until they have run. Measured on this
JDK (Java 25), with one task due in ten seconds:

```
executeExistingDelayedTasksAfterShutdown=true   terminated=false waited=2000ms queue=1
executeExistingDelayedTasksAfterShutdown=false  terminated=true  waited=0ms    queue=0
```

Left at the default, a single pending one-shot task spends the entire 20-second budget
waiting for something nobody needs — and `SolrScheduledReindexService`'s startup retries
are exactly that: scheduled directly rather than through `@Scheduled`, up to four minutes
apart, and not registered with Spring's `ScheduledTaskRegistrar`, so nothing else cancels
them. Worse, keeping them lets a retry *start* a rebuild during shutdown, which is dri-one
#355's partial index with extra steps.

`SchedulingConfig` therefore contributes a `ThreadPoolTaskSchedulerCustomizer` setting
`executeExistingDelayedTasksAfterShutdownPolicy(false)`. Boot exposes no property for it. A
task already **running** is unaffected — the queue is dropped, and awaiting termination is
what gives the running one time to finish.

### Why 20 seconds

The await has to fit inside the container's termination grace period, or Kubernetes
`SIGKILL`s the pod part-way through the task and the wait bought nothing:

| | |
|---|---|
| `terminationGracePeriodSeconds` (prod, staging) | unset → Kubernetes default **30s** |
| preStop hook | none |
| `spring.task.scheduling.shutdown.await-termination-period` | **20s** |

Twenty leaves ten seconds for the rest of the context to close and the JVM to exit. Boot
truncates the value to whole seconds (`setAwaitTerminationSeconds`), so sub-second
precision here is meaningless. If either the grace period or the pool size changes, revisit
this number: more threads means more work sharing the same total budget.

Note also that `docker stop` defaults to a **10-second** grace, shorter than the await —
pass `-t 30` when testing locally, or the local result will not resemble production.

## What none of this covers

**`SIGKILL` skips the shutdown path entirely.** An OOM kill, a node preemption, a
`kubectl delete pod --force`, or exceeding the grace period leaves the JVM no shutdown hook
at all, so nothing on this page applies. The exposure is per task, and it is the same list
as the table above: a `syncDB` killed mid-window loses up to an hour of counts, a killed
keyword-vocabulary refresh loses a week of that vocabulary, a killed `git gc` can take the
catalogue down. Nothing in this application currently makes any of those
crash-safe; the only levers for the `SIGKILL` case are memory headroom (so the OOM kill
does not happen) and shorter cadences (so less is at stake per run). dri-one #245 reached
the same conclusion for the metrics flush specifically.

**Interruption is not a guaranteed stop, either.** Before this change the running task was
interrupted, but interruption only aborts *interruptible* waits — `Thread.sleep`, `Object.wait`,
NIO channel operations, and the future `get()` calls inside Solr's HTTP client. A plain
blocking socket read or a `FileOutputStream.write` is not aborted by it, so a task could
keep running while the rest of the context closed around it, on a scheduler thread that is
not a daemon. "Interrupted" therefore meant *undefined*, not *stopped* — another reason to
prefer awaiting.

**Virtual threads are a separate decision.** `spring.threads.virtual.enabled` would replace
the pool with a `SimpleAsyncTaskScheduler` and make `pool.size` inert (the property's own
javadoc says so), but it also changes every other executor in the application — the Tomcat
request pool and the one `@Async` method on `DataciteIndexingService` included. It is not
folded in here on purpose; it should be judged on its own merits.

## Verifying it for real

The wiring is exactly the kind of thing that looks fixed in a test and is not, so this was
checked at both levels: in-process regression tests, and a real container taking a real
SIGTERM.

The tests are `SchedulingConfigTest`, which loads the shipped `application.properties` and
asserts the bound values, that a task running when the scheduler shuts down finishes
uninterrupted, and that a queued one is dropped rather than awaited; plus one assertion in
`EidcApplicationContextTest` that the production context's scheduler really is the
auto-configured pool, since the property governs nothing if anything else defines a
`TaskScheduler` bean. Deleting the properties fails three of those; removing the customizer
fails the fourth and makes the run 20 seconds slower, which is the behaviour it exists to
prevent.

### Why the test suite overrides both properties

`application-test.properties` and `uk/ac/ceh/gateway/catalogue/test.properties` both put
`pool.size` back to 1 and `await-termination` back to false. That is deliberate, and it is not
a hedge against the production values — do not delete it to "make the tests match production".

`@EnableScheduling` sits on `CatalogueApplication`, so every `@SpringBootTest` in this
repository gets a real scheduler with the real `@Scheduled` methods attached, and Spring caches
those contexts for the lifetime of the test JVM. Every ungated entry in the table above has an
`initialDelay` between one and five minutes — shorter than a suite run — so the GEMET, ROR and
SPARQL vocabulary retrievals, the Jena and MapServer reindex checks and the Turtle prefetch all
genuinely execute during the tests, against their real endpoints. That leak is much older than
this change and is not fixed here; what the overrides do is stop the production values turning
it into a build failure. With the shipped values, CI showed both halves of that:

- a pool of four lets four of those tasks run at once in each cached context, and a Gradle test
  worker gets 512m by default, so the suite exhausted the heap — surfacing as an
  `OutOfMemoryError` inside an unrelated context load (`OnlineResourceControllerTest`), which
  then failed every test sharing that context;
- awaiting termination replaces `shutdownNow()` with a 20-second wait per context close, and
  those retrievals sit blocked on network reads that never complete in CI, so every one of the
  ~25 cached contexts burned the full budget as the shutdown hook closed them — 8m20s of pure
  waiting, taking `test_java` from 179s to 1004s.

The shipped values keep their coverage regardless, because `SchedulingConfigTest` reads the real
`application.properties` through `ConfigDataApplicationContextInitializer` with no profile
active, so neither override is visible to it.

The real fix for the underlying leak is to stop ungated `@Scheduled` beans doing outbound work in
test contexts at all — `SolrScheduledReindexService` already shows the pattern with
`@Profile("!test")`. That is a larger change than this one and has not been made yet; until it is,
these overrides are what keeps the suite honest about it.

### The container check

`docker compose` is the wrong vehicle: the dev container runs `./gradlew :java:bootRun`, so
PID 1 is Gradle and the application is a forked child — signal delivery is Gradle's to
decide, not the JVM's. The `prod` stage is the right target, and its entrypoint is the JVM
itself. (The `dev` stage would be more convenient, since it seeds a datastore, but its
build is currently broken for an unrelated reason: it runs `apk add git vim` after `prod`
has already switched to `USER spring`.) So seed a datastore by hand — the same thing the
`datastore` build stage does:

```bash
docker build --target prod -t catalogue-shutdown-probe .

cp -r fixtures/datastore/REV-1 /tmp/ds && cd /tmp/ds \
  && git init -q -b main && git add -A && git commit -qm "data loading" \
  && chmod -R a+rwX /tmp/ds

docker run -d --name shutdown-probe \
  -v /tmp/ds:/var/ceh-catalogue/datastore \
  --env-file secrets.env \
  -e SPRING_PROFILES_ACTIVE=development,server-eidc,search-basic,service-agreement,upload-simple \
  -e LOGGING_LEVEL_ROOT=info \
  catalogue-shutdown-probe

# 30s grace, matching Kubernetes. docker stop defaults to 10s, which is shorter than the
# await period, so the default would not resemble production.
time docker stop -t 30 shutdown-probe
docker logs shutdown-probe | grep -oE "\[ *scheduling-[0-9]+\]" | sort | uniq -c
```

Run on 2026-09-03 against this commit, with no Solr reachable (so the startup retry chain
was mid-backoff and a one-shot task was pending in the queue):

```
      3 [   scheduling-1]        # pool of four, all of it in use
      5 [   scheduling-2]
      3 [   scheduling-3]
     33 [   scheduling-4]

15:13:22.967   SIGTERM sent
15:13:23.019   Commencing graceful shutdown ... Graceful shutdown complete  (Tomcat, 6ms)
15:13:25.146   last log line from the task still running on scheduling-1
15:13:25.620   process exited 143
```

Which is the whole change in one trace: the task on `scheduling-1` kept working for 2.2
seconds after the signal and the JVM waited for it (no `InterruptedException` anywhere in
the log), while the Solr retry due 60 seconds later was dropped rather than awaited — had
it been kept, the stop would have taken the full 20 seconds instead of 2.6.

The same run with Boot's defaults restored (`SPRING_TASK_SCHEDULING_POOL_SIZE=1`,
`SPRING_TASK_SCHEDULING_SHUTDOWN_AWAIT_TERMINATION=false`) put all 207 scheduled log lines
on `scheduling-1`.

In the cluster the equivalent is `kubectl -n eidc delete pod <pod>` with `kubectl logs -f`
on the terminating pod.

What to look for:

- Scheduled work spread across `scheduling-1` … `scheduling-4` rather than all on
  `scheduling-1`.
- No `InterruptedException` from a scheduled task at shutdown.
- A prompt exit when nothing is running. A `Timed out while waiting for executor
  'taskScheduler' to terminate` warning after 20 seconds means either something *was* still
  running (fine — that is the setting working) or that a pending task is being awaited (not
  fine — check the customizer in `SchedulingConfig` is still in place).
