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

  ${header.numFound} records found

  <#list results as result>
    <div class="result row">
        <div class="col-md-1">
            <span class="label label-Dataset">Dataset</span>
        </div>
        <div class="col-md-11">
            <a href="/documents/${result.identifier}">${result.title}</a>
                ${result.description}
        </div>
    </div>
  </#list>

</@skeleton.master>