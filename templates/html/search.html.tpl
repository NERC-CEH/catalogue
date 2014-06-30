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
  <!--http://jsfiddle.net/22cyX/-->
  <div class="col-md-2 facet">
      <#list facets as facet>
        <div class="well">
          <h3>${facet.displayName}</h3>
        </div>
        <ul class="nav nav-pills nav-stacked">
          <#list facet.results as result>
              <li class="active">
                <a href="#">
                  ${result.name} (${result.count})
                      <span class="badge pull-right">+</span>
                </a>
             </li>
           </#list>
        </ul>    
  </div>

  <div class="col-md-offset-1 col-md-9">
    <div class="row">
        ${header.numFound} records found
    </div>
  
    <#list results as result>
      <div class="result row">
          <div class="col-md-2">
              <span class="label label-Dataset">Dataset</span>
          </div>
          <div class="col-md-7">
              <a href="/documents/${result.identifier}">${result.title}</a>
                  ${result.description}
          </div>
      </div>
    </#list>
  </div>

</div>

</@skeleton.master>