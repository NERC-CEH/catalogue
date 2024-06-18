<#if metrics??>
  <#assign
    totalViews = metrics.totalViews(id)
    totalDownloads = metrics.totalDownloads(id)>
</#if>


<div class="metrics">
  <div>
    <#if incomingCitations?has_content && catalogue.id=="eidc">
      <#if incomingCitationCount gt 0 >
      <div class="metric">
        <a href="#section-citations">
        <i class="fa-solid fa-book-open-reader fa-fw fa-lg"></i>
        <span>${incomingCitationCount}</span> citation<#if incomingCitationCount gt 1>s</#if>
        </a>
      </div>
      </#if>
    </#if>
    <#if totalDownloads?? && totalDownloads gt 0 >
      <div class="metric">
        <i class="fa-solid fa-book-open-reader fa-fw fa-lg"></i>
        <span>${totalDownloads}</span> download<#if totalDownloads != 1>s</#if>
      </div>
    </#if>
    <#if totalViews?? && totalViews gt 0 >
      <div class="metric">
        <i class="fa-solid fa-eye fa-fw fa-lg"></i>
        <span>${totalViews}</span> view<#if totalViews != 1>s</#if>
      </div>
    </#if>
  </div>
</div>
