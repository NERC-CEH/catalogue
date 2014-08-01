<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Search">

<div class="row">

  <!--http://jsfiddle.net/22cyX/-->
  <div class="col-md-3 well">
    <div class="search-container">
      <div class="search-box">
        <form id="search-form" action="/documents" method="get">
          <@addFacetFiltersToForm header.facetFilters/>
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

      <ul class="nav nav-pills nav-stacked">
        <#list facets as facet>
          <li class="facet-heading">
            <!-- <a href="#"> -->
              ${facet.displayName}
            </a>
          </li>

          <#list facet.results as result>
            <#if isActiveFacetFilter(header.facetFilters facet.fieldName result.name)>
              <li class="facet-filter-active"}>
                <a href="/documents?term=${term}${removeFacetFilter(header.facetFilters, facet.fieldName + '|' + result.name)}" class="facet-link-active">
                  <span class="facet-result-name">${result.name}</span>
                  <span class="glyphicon glyphicon-remove-circle pull-right"></span>
                </a>
              </li>
            <#else>
              <li class="facet-filter-inactive">
                <a href="/documents?term=${term}&facet=${facet.fieldName}|${result.name}${getFacetFiltersAsQueryParams(header.facetFilters)}" class="facet-link-inactive">
                  <span class="facet-result-name">${result.name}</span> (${result.count})
                </a>
              </li>
            </#if>
          </#list>
        </#list>   
      </ul> 
    </div>
  </div>

  <div class="col-md-9 results-container">
    <div class="row">
      <div class="col-md-12">
        <span id="num-records">${header.numFound}</span> records found
      </div>
    </div>
  
    <#list results as result>
      <div class="result row">
        <div class="col-md-2">
          <span class="label label-${result.resourceType}">${result.resourceType}</span>
        </div>
        <div class="col-md-10">
          <a href="/documents/${result.identifier}" class="search-result-title">${result.title}</a>
            ${result.shortenedDescription}
        </div>
      </div>
    </#list>
  </div>

</div>

</@skeleton.master>

<#function isActiveFacetFilter facetFilters facetFieldName facetValue>
  <#return facetFilters?seq_contains(facetFieldName + "|" + facetValue)>
</#function>

<#function getFacetFiltersAsQueryParams facetFilters firstQueryStringCharacter='&'>
  <#if facetFilters?? && (facetFilters?size > 0)>
    <#return firstQueryStringCharacter + 'facet=' + facetFilters?join('&facet=')>
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
  <#return getFacetFiltersAsQueryParams(toReturn)>
</#function>

<#macro addFacetFiltersToForm facetFilters>
  <#if facetFilters?? && (facetFilters?size > 0)>
    <#list facetFilters as facetFilter>
      <input type='hidden' name='facet' value='${facetFilter}'>
    </#list>
  </#if>
</#macro>