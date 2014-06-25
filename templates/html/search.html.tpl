<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Search">


  <div class="row well">
    <form id="search-form" action="/documents" method="get">
      <div class="input-group">
      
        <input type="text" class="form-control" placeholder="Search the Catalogue" id="term" name="term" value="${header.term}">
        <div class="input-group-btn">
          <button type="submit" id="Search" class="btn btn-success"><span class="glyphicon glyphicon-search"></span></button>
        </div>
      </div>
    </form>
  </div>


<div class="row">
  
  <div class="col-md-2">
      <#list facets as facet>
      <div class="row">
        ${facet.displayName}
        <#list facet.results as result>
          <div class="row">
            ${result.name} : ${result.count}
          </div>
        </#list>
      </div>
      </#list>
  </div>

  <div class="col-md-10">
    <div class="row">
        ${header.numFound} records found
    </div>
  
    <#list results as result>
      <div class="result row">
          <div class="col-md-1">
              <span class="label label-Dataset">Dataset</span>
          </div>
          <div class="col-md-9">
              <a href="/documents/${result.identifier}">${result.title}</a>
                  ${result.description}
          </div>
      </div>
    </#list>
  </div>

</div>

</@skeleton.master>