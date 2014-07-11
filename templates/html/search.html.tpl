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
  <div class="col-md-3 facets">
    <ul class="nav nav-pills nav-stacked">
      <#list facets as facet>
              <li class="active facet">
                <a href="#">
                  ${facet.displayName}
                </a>
             </li>

          <#list facet.results as result>
                <#if isActiveFacetFilter(header.facetFilters facet.fieldName result.name)>
                  <li class="facet-filter-active"}>
                    <a href="/documents?term=${term}${removeFacetFilter(header.facetFilters, facet.fieldName + '|' + result.name)}">
                      <span class="facet-result-name">${result.name}</span>
                      <span class="badge pull-right facet-result-count">X</span>
                    </a>
                 </li>
                <#else>
                  <li class="facet-filter-inactive">
                    <a href="/documents?term=${term}&facet=${facet.fieldName}|${result.name}${getActiveFacetFiltersForUrl(header.facetFilters)}">
                      <span class="facet-result-name">${result.name} (${result.count})</span>
                    </a>
                 </li>
                </#if>
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

<#function isActiveFacetFilter facetFilters facetFieldName facetValue>
  <#return facetFilters?seq_contains(facetFieldName + "|" + facetValue)>
</#function>

<#function getActiveFacetFiltersForUrl facetFilters>
  <#if facetFilters?? && (facetFilters?size > 0)>
    <#return '&facet=' + facetFilters?join('&facet=')>
  <#else>
    <#return ''>
  </#if>
</#function>

<#function removeFacetFilter facetFilters toRemove>
  <#assign toReturn = []>
  <#list facetFilters as facetFilter>
    <#if facetFilter != toRemove>
      <#assign toReturn = toReturn + [facetFilter]>
    </#if>
  </#list>
  <#return getActiveFacetFiltersForUrl(toReturn)>
</#function>