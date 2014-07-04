<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Search">


  <div class="row">
  <div class="col-md-12 well">
    <form id="search-form" action="/documents" method="get">
      <div class="input-group">
        <#if header.term="*">
          <#assign term="">
        <#else>
          <#assign term=header.term>
        </#if>
        <input type="text" class="form-control" placeholder="Search the Catalogue" id="term" name="term" value="${term}">
        <div class="input-group-btn">
          <button type="submit" id="Search" class="btn btn-success"><span class="glyphicon glyphicon-search"></span></button>
        </div>
      </div>
    </form>
  </div>
</div>


<div class="row">
  <!--http://jsfiddle.net/22cyX/-->
  <div class="col-md-3 facet">
    <ul class="nav nav-pills nav-stacked">
      <#list facets as facet>
              <li class="active">
                <a href="#">
                  ${facet.displayName}
                </a>
             </li>

          <#list facet.results as result>
              <li>
                <a href="#">
                  ${result.name}
                      <span class="badge pull-right">${result.count}</span>
                </a>
             </li>
           </#list>
      </#list>   
    </ul> 
  </div>

  <div class="col-md-9">
    <div class="row">
      <div class="col-md-12">
        ${header.numFound} records found
      </div>
    </div>
  
    <#list results as result>
      <div class="result row">
          <div class="col-md-2">
              <span class="label label-${result.resourceType}">${result.resourceType}</span>
          </div>
          <div class="col-md-10">
              <a href="/documents/${result.identifier}" class="search-result-title">${result.title}</a>
                  ${result.description}
          </div>
      </div>
    </#list>
  </div>

</div>

</@skeleton.master>