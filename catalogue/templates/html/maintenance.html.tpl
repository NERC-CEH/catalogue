<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Maintenance Page">
  <div class="container">
    <h1>Catalogue Maintenance</h1>

    <#if messages?has_content>
      <div class="alert alert-info">
        <h2>Messages</h2>
        <ol>
          <#list messages as message>
            <li>${message}</li>
          </#list>
        </ol>
      </div>
    </#if>

    <#if latestRevision??>
      <h2>Latest Git Commit</h2>
      <dl>
        <dt>Latest Revision</dt><dd>${latestRevision.revisionID}</dd>
        <dt>Author</dt><dd>${latestRevision.author.email}</dd>
        <dt>Message</dt><dd>${latestRevision.message}</dd>
        <#if lastOptimized??>
          <dt>Last Optimized</dt><dd>${lastOptimized?datetime}</dd>
        </#if>
      </dl>
      <form method="POST" action="/maintenance/documents/optimize">
        <input type="submit" value="Optimize">
      </form>
    </#if>

    <h2>Indexing</h2>
    <dl>
      <dt>Has Solr Index?</dt><dd>${indexed?string("Yes", "No")}
        <form method="POST" action="/maintenance/documents/reindex">
          <input type="submit" value="Rebuild">
        </form>
      </dd>
      <dt>Has Links?</dt><dd>${linked?string("Yes", "No")}
        <form method="POST" action="/maintenance/links/reindex">
          <input type="submit" value="Rebuild">
          <a href="/maintenance/sparql">Sparql tester</a>
        </form>
      </dd>
    </dl>

    <h2>Upload a terraCatalog export</h2>
    <form method="POST" enctype="multipart/form-data" action="/maintenance/terraCatalogImport">
      <input type="file" name="file">
      <input type="submit" value="Import">
    </form>
  </div>
</@skeleton.master>
