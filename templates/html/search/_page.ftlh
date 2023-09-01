<#--
  This freemarker fragment generates the search results as static HTML content
  from the catalogue api.

  IMPORTANT: If you change the structure of this, please update the corresponding
  javascript template /web/src/scripts/templates/SearchPage.tpl
-->

  <div class="results__header">
    <span id="num-records">${numFound}</span> records found
  </div>

  <#if relatedSearches?? && relatedSearches?has_content>
    <div class="results__related_searches">
      <h2>Related Searches</h2>
      <#list relatedSearches>
        <#items as relatedSearch>
          <a href="${relatedSearch.href}">${relatedSearch.title}</a><#if relatedSearch_has_next>, </#if>
        </#items>
      </#list>
    </div>
  </#if>

  <div class="results__list">
    <#list results as result>
      <a href="/${docroot}/${result.identifier}" class="result result--${result.state} <#if (result.resourceStatus??)>result--${result.resourceStatus}</#if>" data-location="${(result.locations?join('|'))!}" id="${result.identifier}">

        <div class="result__tags">
          <div class="recordType">
          <#if result.documentType?? && result.documentType == "LINK_DOCUMENT" >
            <i class="fa-solid fa-link"></i> Linked
          </#if>
          ${result.recordType!""}
          </div>

          <#if result.resourceStatus?? >
              <div class="resourceStatus">${result.resourceStatus}</div>
          </#if>

          <div class="state">${codes.lookup('publication.state', result.state)?upper_case!''}</div>

          <#if result.condition?? >
            <div class="condition">${result.condition}</div>
          </#if>

        </div>

        <div class="result__title">${result.title}</div>
        <div class="result__description">${result.shortenedDescription}</div>

        <#if result.incomingCitationCount gt 0 >
          <div class="result__citationCount">${result.incomingCitationCount} citation<#if result.incomingCitationCount gt 1 >s</#if></div>
        </#if>

      </a>
    </#list>
  </div>

  <div class="results__footer">
    <ul class="pager">
      <#if prevPage?has_content>
        <li class="previous"><a href="${prevPage}">&larr; Previous</a></li>
      </#if>
      <li>Page ${page}</li>
      <#if nextPage?has_content>
        <li class="next"><a href="${nextPage}">Next &rarr;</a></li>
      </#if>
    </ul>
  </div>
