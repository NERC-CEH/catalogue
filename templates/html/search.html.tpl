<#import "skeleton.html.tpl" as skeleton>
<#assign docroot="documents">
<@skeleton.master title="Search">
<div class="container">
  <div id="layout">
    <div class="search">
      <form id="search-form" action="/${docroot}" method="get">
        <#if facetFilters??>
          <#list facetFilters as facetFilter>
            <input type='hidden' name='facet' value='${facetFilter}'>
          </#list>
        </#if>
        <div class="input-group">
          <input type="text" class="form-control" placeholder="Search the Catalogue" id="term" name="term" value="${term}">
          <div class="input-group-btn">
            <button type="submit" id="Search" class="btn btn-success"><span class="glyphicon glyphicon-search"></span></button>
          </div>
        </div>
      </form>
      <ul class="facets">
        <#list facets?sort_by("displayName")?reverse as facet>
          <li class="facet-heading">${facet.displayName}</li>
          <#list facet.results as result>
            <li class="facet-filter-${result.state!'inactive'}">
              <@facetUrl result.url result.name result.count result.state />
              <#if result.subFacetResults?? && result.subFacetResults?has_content>
                <ul class="facets sub">
                  <#list result.subFacetResults as sub>
                    <li class="facet-filter-${sub.state}">
                      <@facetUrl sub.url sub.name sub.count sub.state />
                    </li>
                  </#list>
                </ul>
              </#if>
            </li>
          </#list>
        </#list>
      </ul>
    </div>
    <div class="results">
      <div class="search-results-heading">
        <span id="num-records">${numFound}</span> records found
      </div>
      <#list results as result>
        <div class="result">
          <h2>
            <a href="/${docroot}/${result.identifier}" class="title">${result.title}</a>
            <div class="resource-type ${result.resourceType}">${result.resourceType}</div>
          </h2>
          <div class="description">${result.shortenedDescription}</div>
        </div>
      </#list>
    </div>
  </div>
</div>
</@skeleton.master>
<#macro facetUrl url name count state>
<a href="${url}">
  <span class="facet-result-name">${name} <#if state=='inactive'>(${count})</#if></span>
  <span class="facet-result-remove"></span>
</a>
</#macro>