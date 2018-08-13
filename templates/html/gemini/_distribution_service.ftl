<#if onlineResources?? >
  <#assign mapservices = func.filterRegex(onlineResources, "url", "https://catalogue.ceh.ac.uk/maps/")>
</#if>

  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading"><p class="panel-title">Use this service</p></div>
    <div class="panel-body">
      <div class="distribution-service">
        <p><a href="${mapViewerUrl?html}"><i class="far fa-map text-info"></i> Open in map viewer</a></p>
        <#if mapservices??>
        <#list mapservices as wms>
          <p><i class="fas fa-link"></i> URL: <span class="wmsurl">${wms.url?html}</span></p>
        </#list>
        </#if>
      </div>
    </div>
  </div>