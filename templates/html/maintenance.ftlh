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
