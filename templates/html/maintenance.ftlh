<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="Maintenance Page" catalogue=catalogues.defaultCatalogue()>
  <div class="container">
    <h1 class="my-4">Catalogue Maintenance</h1>

    <#if messages?has_content>
      <div class="alert alert-info">
        <h2 class="mt-5 mb-2">Messages</h2>
        <ol>
          <#list messages as message>
            <li>${message}</li>
          </#list>
        </ol>
      </div>
    </#if>

    <#if latestRevision??>
      <h2 class="mt-5 mb-2">Latest Git Commit</h2>
      <dl>
        <dt>Latest Revision</dt><dd>${latestRevision.revisionID}</dd>
        <dt>Author</dt><dd>${latestRevision.author.email}</dd>
        <dt>Message</dt><dd>${latestRevision.message}</dd>
        <#if lastOptimized??>
          <dt>Last Optimized</dt><dd>${lastOptimized?datetime}</dd>
        </#if>
      </dl>
      <form method="POST" action="/maintenance/documents/optimize">
        <input class="btn btn-outline-secondary" type="submit" value="Optimize">
      </form>
    </#if>

    <div class="mt-5">
      <h2>Indexing</h2>

      <div class="my-5">
        <p><b>Has Solr Index?</b> <span class="ms-5">${indexed?string("Yes", "No")}</span></p>
        <form method="POST" action="/maintenance/documents/reindex">
          <input class="btn btn-outline-secondary" type="submit" value="Rebuild">
        </form>
      </div>

      <div class="my-5">
        <p><b>Has Links?</b> <span class="ms-5">${linked?string("Yes", "No")}</span></p>
        <form method="POST" action="/maintenance/links/reindex">
          <input class="btn btn-outline-secondary" type="submit" value="Rebuild">
          <a href="/maintenance/sparql">Sparql tester</a>
        </form>
      </div>

      <div class="my-5">
        <p><b>Has MapFiles?</b> <span class="ms-5">${hasMapFiles?string("Yes (${indexedMapFilesCount})", "No")}</span></p>
        <form method="POST" action="/maintenance/mapfiles/reindex">
          <input class="btn btn-outline-secondary" type="submit" value="Rebuild">
        </form>
      </div>

      <#-- Only advertised when the exports profile is active, i.e. CatalogueExportService exists. -->
      <#if exportsAvailable>
        <div class="my-5">
          <p><b>Fuseki Export</b>
            <span class="ms-5">
              <#if lastExported??>
                Last exported ${lastExported?datetime}
              <#else>
                Never exported
              </#if>
            </span>
          </p>
          <form method="POST" action="/maintenance/exports/fuseki">
            <input class="btn btn-outline-secondary" type="submit" value="Export to Fuseki">
          </form>
        </div>
      </#if>
    </div>

    <#-- Only populated under the exports profile: without it there are no SourceGraphProviders
         to declare a graph, so the whole section stays off the page.

         row.graph() not row.graph: GraphProgress is a record, so its accessors do not follow the
         getX() convention and FreeMarker exposes them as methods. Same as templates/rdf/void.ftl. -->
    <#if sourceGraphProgress?has_content>
      <div class="mt-5">
        <h2>Source graphs</h2>
        <p>
          How far each authority's graph has got towards being published. A graph is written whole
          or not at all, so one that is still filling publishes nothing until it is complete &mdash;
          that is normal for a first fill, and a fault only once it stops improving.
        </p>
        <table class="table">
          <thead>
            <tr>
              <th scope="col">Authority</th>
              <th scope="col" class="text-end">Described</th>
              <th scope="col" class="text-end">Outstanding</th>
              <th scope="col">State</th>
            </tr>
          </thead>
          <tbody>
            <#list sourceGraphProgress as row>
              <tr>
                <th scope="row" class="fw-normal">
                  <span title="${row.graph()}">${row.title()}</span>
                </th>
                <#if row.hasCounts()>
                  <td class="text-end">${row.described()} / ${row.entities()}</td>
                  <td class="text-end">${row.outstanding()}</td>
                <#else>
                  <td class="text-end text-muted">&mdash;</td>
                  <td class="text-end text-muted">&mdash;</td>
                </#if>
                <td<#if row.warning()> class="text-danger"</#if>>${row.state()}</td>
              </tr>
            </#list>
          </tbody>
        </table>
        <p class="text-muted">
          Counts are held in memory and are cleared when the application restarts, so every graph
          reads as not yet run until the next export. They describe the last run to reach each
          graph, not what the endpoint currently serves.
        </p>
      </div>
    </#if>

    <#-- Only populated under the vector-search profile, and dropped if the counts could not be
         read at all, so the page never claims a coverage it does not know. Accessors need their
         parentheses: EmbeddingProgress is a record, so FreeMarker exposes them as methods.

         ?c prints the numbers without grouping separators, which keeps the output the same
         whatever locale the JVM happens to be running under. -->
    <#if embeddingProgress??>
      <div class="mt-5">
        <h2>Semantic search</h2>
        <p>
          Semantic search matches records by meaning rather than by keyword, which needs each record
          to carry a vector embedding. A record without one is still found by keyword search, but
          can never be returned by a semantic one.
        </p>
        <table class="table">
          <thead>
            <tr>
              <th scope="col">Records embedded</th>
              <th scope="col" class="text-end">Queued</th>
              <th scope="col">State</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <th scope="row" class="fw-normal">
                ${embeddingProgress.embedded()?c} / ${embeddingProgress.total()?c}
                (${embeddingProgress.percentage()?c}%)
              </th>
              <td class="text-end">${embeddingProgress.pending()?c}</td>
              <td<#if embeddingProgress.warning()> class="text-danger"</#if>>${embeddingProgress.state()}</td>
            </tr>
          </tbody>
        </table>
        <#if embeddingProgress.abandoned() gt 0>
          <p class="text-danger">
            These have been given up on after repeated failures and will not be retried on their
            own. They stay keyword-searchable but have no vector &mdash; save them again, or rebuild
            the index, to retry.
          </p>
        <#else>
          <p class="text-muted">
            Queued records are embedded a batch at a time on a timer, so a count that is still
            filling after a reindex is normal. Counts come from the search index, so they survive a
            restart.
          </p>
        </#if>
      </div>
    </#if>

    <#-- Only advertised to holders of ROLE_CIG_ADMIN_DELETE. The page itself is @Secured, so this is
         about not offering a capability the user does not have. -->
    <#if permission.userCanAdminDelete()>
      <div class="mt-5">
        <h2>Records</h2>
        <div class="my-5">
          <p>
            Delete a record in any catalogue, ignoring its own permissions. For records orphaned by a
            retired document type or catalogue.
          </p>
          <a class="btn btn-outline-danger" href="/maintenance/documents/delete">Delete a record</a>
        </div>
      </div>
    </#if>

  </div>
</@skeleton.master>
