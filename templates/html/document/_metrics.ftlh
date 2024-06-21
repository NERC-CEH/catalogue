<#if metrics??>
  <#assign
    totalViews = metrics.totalViews(id)
    totalDownloads = metrics.totalDownloads(id)>
</#if>

<#if metadata.catalogue == "eidc">
  <#if incomingCitations?has_content || (totalDownloads?? && totalDownloads gt 0) || (totalViews?? && totalViews gt 0)>
    <div class="usage">
      <div class="metrics">
        <#if incomingCitations?has_content && catalogue.id=="eidc">
          <#if incomingCitationCount gt 0 >
          <div class="metric">
            <span><a href="#section-citations">${incomingCitationCount}</a></span> <a href="#section-citations">citation<#if incomingCitationCount gt 1>s</#if></a>
          </div>
          </#if>
        </#if>
        <#if totalDownloads?? && totalDownloads gt 0 >
          <div class="metric">
            <span>${totalDownloads}</span> download<#if totalDownloads != 1>s</#if>
          </div>
        </#if>
        <#if totalViews?? && totalViews gt 0 >
          <div class="metric">
            <span>${totalViews}</span> view<#if totalViews != 1>s</#if>
          </div>
        </#if>
      </div>
      <p><a href="https://eidc.ac.uk/help/metrics" target="_blank" rel="noopener noreferrer" ><small>What does this mean?</small></a></p>
    </div>
  </#if>
</#if>
