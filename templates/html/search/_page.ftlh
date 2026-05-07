<#--
  This freemarker fragment generates the search results as static HTML content
  from the catalogue api.

  IMPORTANT: If you change the structure of this, please update the corresponding
  javascript template /web/src/scripts/templates/searchPage.js
-->
<div class="d-flex p-1 mx-3 results__header align-items-center justify-content-between">
  <#if numFound gt 0>
    <div class="small">
      <#assign startItem = (page - 1) * rows + 1>
      <#assign endItem = (page * rows > numFound)?then(numFound, page * rows)>
      <span id="num-records" class="recordCount">
        Showing <span class="recordCount-start">${startItem}</span> - <span class="recordCount-end">${endItem}</span>  of <span class="recordCount-total">${numFound}</span> records
      </span>
      <i id="searchShareIcon" class="ms-3 fa-solid fa-fw fa-share-square mx-1" data-bs-toggle="tooltip" data-bs-placement="right" role="button"  title="Email search results"></i>
    </div>
    <div class="small">
      <label for="sort-search">Sort by</label>
        <select class="sort-search" id="sort-search" aria-label="Select dropdown for sorting serch results">
          <option value="">Relevance</option>
          <option class="option-eidc" value="publicationDate-desc">Published date (newest first)</option>
          <option class="option-eidc" value="publicationDate-asc">Published date (oldest first)</option>
          <option class="option-eidc" value="incomingCitationCount-desc">Number of citatons</option>
          <option value="title-asc">Title (A-Z)</option>
          <option value="title-desc">Title (Z-A)</option>
        </select>
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
  <#assign status = "unknown">
  <#if result.operationalStatus??>
    <#assign status = result.operationalStatus?lower_case>
  </#if>
  <#if result.availability??>
    <#assign status = result.availability?lower_case>
  </#if>

  <a href="/${docroot}/${result.identifier}" class="result result--${result.state} result--${status}" data-location="${(result.locations?join('|'))!}" id="${result.identifier}">

    <div class="result__publicationState">${(codes.lookup("publication.state", result.state)!)!?upper_case!''}</div>

    <div>
      <div class="result__tags">
          <#if (result.operationalStatus??)>
            <span class="opstatus">${result.operationalStatus}</span>
          </#if>

          <span class="recordType small text-body-tertiary">
            <#if result.documentType?? && result.documentType == "LINK_DOCUMENT" >
              <i class="fa-solid fa-link"></i> Linked ${result.recordType?lower_case!""}
            <#else>
              ${result.recordType!""}
            </#if>
          </span>

          <#if catalogue.id == "all">
            <span class="catalogue-label small text-body-tertiary">${result.catalogue!""}</span>
          </#if>

          <#if result.availability?? >
            <span class="availability availability-${result.availability}">${result.availability}</span>
          </#if>
      </div>
      <div class="result__title">${result.title} </div>
      <div class="result__description">${result.shortenedDescription}</div>
    </div>

    <#if result.incomingCitationCount gt 0 >
      <div class="result__citationCount">${result.incomingCitationCount} citation<#if result.incomingCitationCount gt 1 >s</#if></div>
    </#if>

  </a>
</#list>
</div>

<div class="results__footer">
  <#assign totalPage = (numFound / rows)?ceiling>
  <#if totalPage gt 1>
    <#assign pageToShow = 5>
    <#assign halfPageToShow = (pageToShow / 2)?floor>
    <#assign startPage = (page - halfPageToShow > 0)?then(page - halfPageToShow, 1)>
    <#assign endPage = (startPage + pageToShow - 1 > totalPage)?then(totalPage, startPage + pageToShow - 1)>
    <#if (endPage - startPage + 1) < pageToShow && (endPage - pageToShow + 1 > 0)>
      <#assign startPage = endPage - pageToShow + 1>
    </#if>

    <nav aria-label="Search results pagination">
      <ul class="pagination mb-0">
        <#if page gt 1>
          <li class="page-item">
            <a class="page-link" href="${prevPage}" aria-label="Previous">
              <span aria-hidden="true">&laquo;</span>
            </a>
          </li>
        <#else>
          <li class="page-item disabled">
            <span class="page-link" aria-hidden="true">&laquo;</span>
          </li>
        </#if>

        <#list startPage..endPage as i>
          <#if i == page>
            <li class="page-item active" aria-current="page">
              <span class="page-link">${i}</span>
            </li>
          <#else>
            <li class="page-item">
              <a class="page-link" href="${buildPageUrl(url, page, i)}">${i}</a>
            </li>
          </#if>
        </#list>

        <#if page < totalPage>
          <li class="page-item">
            <a class="page-link" href="${nextPage}" aria-label="Next">
              <span aria-hidden="true">&raquo;</span>
            </a>
          </li>
        <#else>
          <li class="page-item disabled">
            <span class="page-link" aria-hidden="true">&raquo;</span>
          </li>
        </#if>
      </ul>
    </nav>
  </#if>
</div>

<#function buildPageUrl url currentPage pageNum>
  <#if url?contains('page=')>
    <#return url?replace('page=' + currentPage, 'page=' + pageNum)>
  <#else>
    <#return url + (url?contains('?')?then('&', '?')) + 'page=' + pageNum>
  </#if>
</#function>
