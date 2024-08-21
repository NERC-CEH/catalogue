<#assign
  totalViews = 0
  totalDownloads = 0>

<#if metrics??>
  <#if metrics.totalViews(id)??>
      <#assign totalViews = metrics.totalViews(id)>
  </#if>
  <#if metrics.totalDownloads(id)??>
    <#assign totalDownloads = metrics.totalDownloads(id)>
  </#if>
</#if>

<#assign downloadUrl = "" >
<#if onlineResources?? >
  <#assign downloadList=downloadOrderDetails.from(onlineResources) >
  <#if downloadList?? && downloadList.orderResources??&& downloadList.orderResources?has_content>
    <#assign downloadUrl = downloadList.orderResources?first.url >
  </#if>
</#if>

<#if incomingCitations?has_content || totalDownloads gt 0 || totalViews gt 0>
  <div class="usage">
    <div class="metrics">
      <#if incomingCitations?has_content && catalogue.id=="eidc">
        <#if incomingCitationCount gt 0 >
        <div class="metric">
          <span><a href="#section-citations">${incomingCitationCount}</a></span> <a href="#section-citations">citation<#if incomingCitationCount gt 1>s</#if></a>
        </div>
        </#if>
      </#if>
      <#if totalDownloads gt 0 >
        <div class="metric">
          <span>
            ${totalDownloads}
            <a href=".metricsInfo" data-toggle="collapse" aria-controls="metricsInfo" area-expanded="false" role="button">*</a>
          </span>
          download<#if totalDownloads != 1>s</#if>
        </div>
      </#if>
      <#if totalViews gt 0 >
        <div class="metric">
          <span>
            ${totalViews}
            <a href=".metricsInfo" data-toggle="collapse" aria-controls="metricsInfo" area-expanded="false" role="button">*</a>
          </span>
          view<#if totalViews != 1>s</#if>
        </div>
      </#if>
    </div>

    <p><a href="https://eidc.ac.uk/help/metrics" target="_blank" rel="noopener noreferrer" ><small>More information about these numbers</small></a></p>

    <#--
    More information about the dates from which data were collected
    Start dates are different for order manager data and data packager data
    -->
      <#if totalViews gt 0 || totalDownloads gt 0 >
        <#assign
          viewsFrom = "2023-06-01"
          ordersFrom = "2021-11-11"
          downloadsFrom = "2024-06-20">

        <#if datasetReferenceDate.publicationDate??>
          <#if (datasetReferenceDate.publicationDate)?date gt viewsFrom?date >
            <#assign viewsFrom=datasetReferenceDate.publicationDate >
          </#if>

          <#if downloadUrl?contains("order-eidc")>
            <#if (datasetReferenceDate.publicationDate)?date gt ordersFrom?date >
              <#assign downloadsFrom=datasetReferenceDate.publicationDate >
            <#else>
              <#assign downloadsFrom=ordersFrom >
            </#if>
          <#else>
            <#if (datasetReferenceDate.publicationDate)?date gt downloadsFrom?date >
              <#assign downloadsFrom=datasetReferenceDate.publicationDate >
            </#if>
          </#if>
        </#if>

        <div class="metricsInfo collapse">
          <#if totalViews gt 0 >
            <span>View numbers valid from ${viewsFrom?date?string['dd MMMM yyyy']}</span>
          </#if>
          <#if totalDownloads gt 0 >
            <span>Download numbers valid from ${downloadsFrom?date?string['dd MMMM yyyy']}</span>
          </#if>
          <span class="small">(information prior to this was not collected)<span>
        </div>
      </#if>
      <#-- END OF more information about the dates -->

  </div>
</#if>
