<#if catalogue.id=="eidc">
<#assign
  totalViews = 1
  totalDownloads = 2

  viewsBaseline = "2023-06-01"
  ordersBaseline = "2021-11-11"
  downloadsBaseline = "2024-06-20"
  downloadUrl = ""
>

  <#if metrics??>
    <#if metrics.totalViews(id)??>
        <#assign totalViews = metrics.totalViews(id)>
    </#if>
    <#if metrics.totalDownloads(id)??>
      <#assign totalDownloads = metrics.totalDownloads(id)>
    </#if>
  </#if>

  <#if onlineResources?? >
    <#assign downloadList=downloadOrderDetails.from(onlineResources) >
    <#if downloadList?? && downloadList.orderResources??&& downloadList.orderResources?has_content>
      <#assign downloadUrl = downloadList.orderResources?first.url >
    </#if>
  </#if>

  <#if resourceType?? && (recordType == 'dataset' || resourceType.value == 'model' || resourceType.value == 'software' || resourceType.value == 'computationalNotebook' || resourceType.value == 'method' || resourceType.value == 'service' )>
    <#if incomingCitations?has_content || totalDownloads gt 0 || totalViews gt 0>

      <div class="usage" id="segment-usage">
        <div>
          <div class="metrics">
            <#if incomingCitations?has_content>
              <#if incomingCitationCount gt 0 >
              <div class="metric">
              <a href="#segment-citations">${incomingCitationCount}
                <span class="small">citation<#if incomingCitationCount gt 1>s</#if></span></a>
              </div>
              </#if>
            </#if>
            <#if totalDownloads gt 0 >
              <div class="metric">
                ${totalDownloads} <span>download<#if totalDownloads != 1>s</#if></span>
              </div>
            </#if>
            <#if totalViews gt 0 >
              <div class="metric">
                  ${totalViews} <span>view<#if totalViews != 1>s</#if></span>
              </div>
            </#if>
          </div>

          <div class="metricsInfo">
              <#assign
                showToggle= false
                toggleText= ""
              >

              <#if totalViews gt 0 >
                <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate??>
                  <#if (datasetReferenceDate.publicationDate)?date lt viewsBaseline?date >
                    <#assign
                      showToggle = true
                      toggleText += "<div>View numbers valid from " + viewsBaseline?date?string['dd MMMM yyyy'] + "</div>"
                    >
                  </#if>
                </#if>
              </#if>

              <#if totalDownloads gt 0 >
                <#assign showToggle = true>
                <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate??>
                  <#if downloadUrl?contains("order-eidc")>
                    <#if (datasetReferenceDate.publicationDate)?date lt ordersBaseline?date >
                    <#assign toggleText += "<div>Download numbers valid from " + ordersBaseline?date?string['dd MMMM yyyy'] + "</div>">
                    </#if>
                  <#else>
                    <#if (datasetReferenceDate.publicationDate)?date lt downloadsBaseline?date >
                    <#assign toggleText += "<div>Download numbers valid from " + downloadsBaseline?date?string['dd MMMM yyyy'] + "</div>">
                    </#if>
                  </#if>
                </#if>
              </#if>

              <#if showToggle >
                <div class="metricsDates">
                  <a href=".metricsDatesMore" class="seemore" data-bs-toggle="collapse" aria-controls="metricsDatesMore" area-expanded="false" role="button" title="Further details about these numbers">
                    NOTES +
                  </a>
                </div>
                <div class="metricsDatesMore collapse">${toggleText?no_esc}</div>
              </#if>

              <div>
                <a href="https://eidc.ac.uk/help/metrics" target="_blank" class="small" rel="noopener noreferrer" >
                  How do we collect this information?
                </a>
              </div>
          </div>

        </div>
      </div>

    </#if>
  </#if>


</#if>
