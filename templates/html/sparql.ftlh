<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="SPARQL querier" catalogue=catalogues.defaultCatalogue()>
    <div class="container">
      <div class="row">
        <form method="POST" action="/maintenance/sparql">
          <div class="col-md-11 col-sm-10 col-xs-12">
            <div class="form-group">
              
              <textarea id="query" class="form-control" rows="10" name="query">
                <#if query??>
                    ${query}
                  <#else>
                    PREFIX dct: <http://purl.org/dc/terms/>
                    PREFIX dcat: <http://www.w3.org/ns/dcat#> 

                    SELECT *
                    WHERE { ?s ?p ?o }
                    LIMIT 10
                </#if>
              </textarea>
            </div>
          </div>
          <div class="col-md-1 col-sm-2 col-xs-12">
            <button type="submit" class="btn btn-default btn-block btn-lg">
              <span class="sr-only">Execute</span>
              <span class="fas fa-play fa-xl"></span>
            </button>
          </div>
        </form>
      </div>
      <div class="row">
        <#if error??><div class="alert alert-danger">${error}</div></#if>
        <#if result??>
          <pre>${result}</pre>
        </#if>
      </div>
    </div>
</@skeleton.master>
