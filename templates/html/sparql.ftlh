<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="Sparql Tester" catalogue=catalogues.defaultCatalogue()>
    <div class="container">
      <form method="POST" action="/maintenance/sparql">
        <textarea id="query" class="my-3 sparqlquery" name="query">
          <#if query??>
            ${query}
          <#else>
            SELECT ?s ?p ?o
            WHERE { ?s ?p ?o }
            LIMIT 10
          </#if>
        </textarea>
        <button type="submit" class="btn btn-outline-secondary">Execute</button>
      </form>

      <#if error??><div class="alert alert-danger">${error}</div></#if>
      <#if result??><div class="sparqlresults"><pre>${result}</pre></div></#if>
    </div>
</@skeleton.master>
