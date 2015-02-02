<#--
  This freemarker fragment generates the search results as static HTML content
  from the catalogue api.

  IMPORTANT: If you change the structure of this, please update the corresponding 
  javascript template /web/src/scripts/templates/SearchPage.tpl
-->
<div class="search-results-heading">
  <span id="num-records">${doc.numFound}</span> records found
</div>
<#list doc.results as result>
  <div class="result" data-location="${result.locations?join(',')}" id="${result.identifier}">
    <h2>
      <a href="/${docroot}/${result.identifier}" class="title">${result.title}</a>
    </h2>
    <div class="description">${result.shortenedDescription}</div>
  </div>
</#list>
<ul class="pager">
  <#if doc.prevPage?has_content>
    <li class="previous"><a href="${doc.prevPage}">&larr; Previous</a></li>
  </#if>
  <li>Page ${doc.page}</li>          
  <#if doc.nextPage?has_content>
    <li class="next"><a href="${doc.nextPage}">Next &rarr;</a></li>
  </#if>
</ul>