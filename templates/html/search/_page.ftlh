<#--
  This freemarker fragment generates the search results as static HTML content
  from the catalogue api.

  IMPORTANT: If you change the structure of this, please update the corresponding
  javascript template /web/src/scripts/templates/searchPage.js
-->
<div class="d-flex p-1 mx-3 results__header align-items-center justify-content-between">
  <#if numFound gt 0>
    <div class="small">
      <span id="num-records">${numFound}</span> record<#if numFound gt 1>s</#if> found
      <i id="searchShareIcon" class="fa-solid fa-fw fa-share-square mx-1" data-bs-toggle="tooltip" data-bs-placement="right" role="button"  title="Email search results"></i>
    </div>
    <div class="small">
      <label>Sort by
        <select class="sort-search" aria-label="Select dropdown for sorting serch results">
          <option value="">Relevance</option>
          <option class="option-eidc" value="publicationDate-desc">Published date (newest first)</option>
          <option class="option-eidc" value="publicationDate-asc">Published date (oldest first)</option>
          <option class="option-eidc" value="incomingCitationCount-desc">Number of citatons</option>
          <option value="title-asc">Title (A-Z)</option>
          <option value="title-desc">Title (Z-A)</option>
        </select>
      </label>
    </div>
  <#else>
    <div>No results found. Try <a href="./documents">clearing all search filters</a></div>
  </#if>
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
    <#if (result.resourceStatus??)>result--${result.resourceStatus}</#if>" data-location="${(result.locations?join('|'))!}" id="${result.identifier}">

    <div class="result__tags">
      <div class="recordType text-body-tertiary">
        <#if result.documentType?? && result.documentType == "LINK_DOCUMENT" >
          <i class="fa-solid fa-link"></i> Linked
        </#if>
        ${result.recordType!""}
      </div>

      <div class="state">${(codes.lookup("publication.state", result.state)!)!?upper_case!''}</div>

      <div class="opstatus">
        <#if (result.operationalStatus??)>
          ${result.operationalStatus}
        </#if>
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
