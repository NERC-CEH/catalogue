<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="Sparql Tester" catalogue=catalogues.defaultCatalogue()>
    <div class="container-fluid">
      <form method="POST" action="/maintenance/sparql">
        <div class="form-group">
          <label for="query">SPARQL Query</label>
          <textarea id="query" class="form-control" rows="14" name="query">
            <#if query??>
              ${query}
            <#else>
              PREFIX prov: <http://www.w3.org/ns/prov#>

              SELECT ?s ?p ?o
              WHERE { ?s ?p ?o }
              LIMIT 10
            </#if>
          </textarea>
        </div>
        <button type="submit" class="btn btn-default">Execute</button>
      </form>

      <#if error??><div class="alert alert-danger">${error}</div></#if>
      <#if result??><code><pre>${result}</pre></code></#if>
    </div>
</@skeleton.master>
