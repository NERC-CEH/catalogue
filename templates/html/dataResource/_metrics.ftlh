<#if catalogue.id=="eidc">
<#assign
  totalViews = 0
  totalDownloads = 0

  viewsBaseline = "2023-06-01"
  ordersBaseline = "2021-11-11"
  downloadsBaseline = "2024-06-20"
  downloadUrl = ""
>

  <#-- One call each: the previous form called both methods twice (once to test, once to assign),
       doubling the queries against a SQLite database on a network share. An unavailable count is
       null, so it falls back to the 0 assigned above and the usage block below is simply omitted. -->
  <#if metrics??>
    <#assign
      totalViews = metrics.totalViews(id)!0
      totalDownloads = metrics.totalDownloads(id)!0
    >
  </#if>

  <#if onlineResources?? >
    <#assign downloadList=downloadOrderDetails.from(onlineResources) >
    <#if downloadList?? && downloadList.dataAccessResources??&& downloadList.dataAccessResources?has_content>
      <#assign downloadUrl = downloadList.dataAccessResources?first.url >
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
                <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate??>
                  <#if (datasetReferenceDate.publicationDate)?date lt downloadsBaseline?date >
                    <#if downloadUrl?contains("order-eidc")>
                      <#if (datasetReferenceDate.publicationDate)?date lt ordersBaseline?date >
                      <#assign
                        showToggle = true
                        toggleText += "<div>Download numbers valid from " + ordersBaseline?date?string['dd MMMM yyyy'] + "</div>">
                      </#if>
                    <#else>
                      <#if (datasetReferenceDate.publicationDate)?date lt downloadsBaseline?date >
                      <#assign
                        showToggle = true
                        toggleText += "<div>Download numbers valid from " + downloadsBaseline?date?string['dd MMMM yyyy'] + "</div>">
                      </#if>
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
