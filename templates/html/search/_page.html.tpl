<#--
  This freemarker fragment generates the search results as static HTML content
  from the catalogue api.

  IMPORTANT: If you change the structure of this, please update the corresponding 
  javascript template /web/src/scripts/templates/SearchPage.tpl
-->
<div class="search-results-heading">
  <span id="num-records">${numFound}</span> records found
</div>
<#list results as result>
  <div class="result" data-location="${(result.locations?join(','))!}" id="${result.identifier}">
    <h2 class="resultTitle">
      <small>
        <span>${result.resourceType!""}</span>
        <#if (result.resourceStatus?? && result.resourceStatus != 'Current') >
          <span class="label-${result.resourceStatus}">(${result.resourceStatus})</span>
        </#if>
        <#if (result.state == 'draft' || result.state == 'pending') >
          <span class="text-danger"><b>${codes.lookup('publication.state', result.state)?upper_case!''}</b> </span>
        </#if>
      </small><br>
      <a href="/${docroot}/${result.identifier}" class="title">${result.title}</a>
    </h2> 
    <div class="resultDescription">${result.shortenedDescription}</div>
  </div>
</#list>
<ul class="pager">
  <#if prevPage?has_content>
    <li class="previous"><a href="${prevPage}">&larr; Previous</a></li>
  </#if>
  <li>Page ${page}</li>
  <#if nextPage?has_content>
    <li class="next"><a href="${nextPage}">Next &rarr;</a></li>
  </#if>
</ul>
