<#--
  This freemarker fragment generates the search results as static HTML content
  from the catalogue api.

  IMPORTANT: If you change the structure of this, please update the corresponding
  javascript template /web/src/scripts/templates/searchPage.js
-->
<div class="results__header">
  <span id="num-records">${numFound}</span> records found
  <span class="mx-2">
    <i id="searchShareIcon" class="fas fa-share-square" data-bs-toggle="tooltip" data-bs-placement="right" role="button" title="Copy search URL to clipboard"></i>
  </span>
</div>

<#list relatedSearches>
  <div class="results__related_searches">
    <h2>Related Searches</h2>
    <#items as relatedSearch>
      <a href="${relatedSearch.href}">${relatedSearch.title}</a><#if relatedSearch_has_next>, </#if>
    </#items>
  </div>
</#list>

<div class="results__list">
<#list results as result>
  <a href="/${docroot}/${result.identifier}"
    class="result result--${result.state}
    <#if (result.operationalStatus??)>opstatus-${result.operationalStatus}</#if>
    <#if (result.resourceStatus??)>result--${result.resourceStatus?lower_case}</#if>" data-location="${(result.locations?join('|'))!}" id="${result.identifier}">

    <div class="result__tags">
      <div class="state">${(codes.lookup("publication.state", result.state)!)!?upper_case!''}</div>

      <div class="opstatus">
        <#if (result.operationalStatus??)>
          ${result.operationalStatus}
        </#if>
      </div>

      <div class="recordType">
        <#if result.documentType?? && result.documentType == "LINK_DOCUMENT" >
          <i class="fa-solid fa-link"></i> Linked
        </#if>
        ${result.recordType!""}
      </div>

      <#if result.resourceStatus?? >
          <div class="resourceStatus">${result.resourceStatus}</div>
      </#if>
    </div>

    <div class="result__title">
      ${result.title}
    </div>

    <div class="result__description">${result.shortenedDescription}</div>

    <#if result.incomingCitationCount gt 0 >
      <div class="result__citationCount">${result.incomingCitationCount} citation<#if result.incomingCitationCount gt 1 >s</#if></div>
    </#if>

  </a>
</#list>
</div>

<div class="results__footer">
<ul class="pagination">
  <#if prevPage?has_content>
    <li class="page-item previous-item"><a class="page-link" href="${prevPage}">&larr; Previous</a></li>
  </#if>
  <li class="page-item center-item">Page ${page}</li>
  <#if nextPage?has_content>
    <li class="page-item next-item"><a class="page-link" href="${nextPage}">Next &rarr;</a></li>
  </#if>
</ul>
</div>
