<#assign
  totalViews = 0
  totalDownloads = 0 >

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

<#if incomingCitations?has_content || totalDownloads gte 0 || totalViews gte 0>
  <div class="usage" id="segment-usage">
    <div>
      <div class="metrics">
        <#if incomingCitations?has_content && catalogue.id=="eidc">
          <#if incomingCitationCount gte 0 >
          <div class="metric">
          <a href="#segment-citations">${incomingCitationCount}
            <span class="small">citation<#if incomingCitationCount gte 1>s</#if></span></a>
          </div>
          </#if>
        </#if>
        <#if totalDownloads gte 0 >
          <div class="metric">
            ${totalDownloads}
            <span>
              download<#if totalDownloads != 1>s</#if>
              <a href=".metricsInfo" data-bs-toggle="collapse" aria-controls="metricsInfo" area-expanded="false" role="button" title="Further details about these numbers">*</a>
            </span>
          </div>
        </#if>
        <#if totalViews gte 0 >
          <div class="metric">
              ${totalViews}
              <span>
                view<#if totalViews != 1>s</#if>
                <a href=".metricsInfo" data-bs-toggle="collapse" aria-controls="metricsInfo" area-expanded="false" role="button" title="Further details about these numbers">*</a>
              </span>
          </div>
        </#if>
      </div>

      <p><a href="https://eidc.ac.uk/help/metrics" target="_blank" rel="noopener noreferrer" ><small>More information</small></a></p>

      <#--
      More information about the dates from which data were collected
      Start dates are different for order manager data and data packager data
      -->
        <#if totalViews gte 0 || totalDownloads gte 0 >
          <#assign
            viewsFrom = "2023-06-01"
            ordersFrom = "2021-11-11"
            downloadsFrom = "2024-06-20">

          <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate??>
            <#if (datasetReferenceDate.publicationDate)?date gte viewsFrom?date >
              <#assign viewsFrom=datasetReferenceDate.publicationDate >
            </#if>

            <#if downloadUrl?contains("order-eidc")>
              <#if (datasetReferenceDate.publicationDate)?date gte ordersFrom?date >
                <#assign downloadsFrom=datasetReferenceDate.publicationDate >
              <#else>
                <#assign downloadsFrom=ordersFrom >
              </#if>
            <#else>
              <#if (datasetReferenceDate.publicationDate)?date gte downloadsFrom?date >
                <#assign downloadsFrom=datasetReferenceDate.publicationDate >
              </#if>
            </#if>
          </#if>

          <div class="metricsInfo collapse">
            <#if totalViews gte 0 >
              <span>View numbers valid from ${viewsFrom?date?string['dd MMMM yyyy']}</span>
            </#if>
            <#if totalDownloads gte 0 >
              <span>Download numbers valid from ${downloadsFrom?date?string['dd MMMM yyyy']}</span>
            </#if>
            <span class="small">(information prior to this was not collected)</span>
          </div>
        </#if>
        <#-- END OF more information about the dates -->
    </div>
  </div>
</#if>
