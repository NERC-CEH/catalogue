<#import "skeleton.ftl" as skeleton>
<@skeleton.master title="Sparql Tester">
  <#escape x as x?html>
    <div class="container-fluid">
      <form method="POST" action="/maintenance/sparql">
        <div class="form-group">
          <label for="comment">SPARQL Query</label>
          <textarea class="form-control" rows="14" name="query">
            <#if query??>
              ${query}
            <#else>
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
  </#escape>
</@skeleton.master>
