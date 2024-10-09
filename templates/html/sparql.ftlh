<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="Sparql Tester" catalogue=catalogues.defaultCatalogue()>
    <div class="container-fluid">
      <form method="POST" action="/maintenance/sparql">
        <div class="mb-3">
          <label for="query">SPARQL Query</label>
          <textarea id="query" class="form-control" rows="14" name="query">
            <#if query??>
              ${query}
            <#else>
              SELECT ?s ?p ?o
              WHERE { ?s ?p ?o }
              LIMIT 10
            </#if>
          </textarea>
        </div>
        <button type="submit" class="btn btn-light border">Execute</button>
      </form>

      <#if error??><div class="alert alert-danger">${error}</div></#if>
      <#if result??><pre>${result}</pre></#if>
    </div>
</@skeleton.master>
