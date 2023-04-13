<#if incomingCitations?has_content && catalogue.id=="eidc">
  <#if incomingCitationCount gt 0 >
    <div id="document-metrics">
      <h2><a href="#section-citations">
      <i class="fa-solid fa-book-open-reader pull-right"></i>
      <span>${incomingCitationCount}</span> citation<#if incomingCitationCount gt 1>s</#if>
      </a></h2>
    </div>
  </#if>
</#if>
