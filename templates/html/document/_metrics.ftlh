<div id="document-metrics">
<#if incomingCitations?has_content && catalogue.id=="eidc">
  <#if incomingCitationCount gt 0 >
    <h2><a href="#section-citations">
    <i class="fa-solid fa-book-open-reader pull-right"></i>
    <span>${incomingCitationCount}</span> citation<#if incomingCitationCount gt 1>s</#if>
    </a></h2>
  </#if>
</#if>
<#if metrics??>
  <#assign totalViews = metrics.totalViews(id) totalDownloads = metrics.totalDownloads(id)>
  ${totalViews} view<#if totalViews != 1>s</#if><br>
  ${totalDownloads} download<#if totalDownloads != 1>s</#if>
  </h2>
</#if>
</div>
