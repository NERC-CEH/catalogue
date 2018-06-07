<#if onlineResources?? >
  <#assign
    downloadOrder=downloadOrderDetails.from(onlineResources) 
    mapservices = func.filterRegex(onlineResources, "url", "https://catalogue.ceh.ac.uk/maps/")>
</#if>

<#if resourceStatus?? && (resourceType.value == 'signpost' || (resourceType.value == 'service' && mapViewable) || resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'application') >
  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading"><p class="panel-title">
      <#if resourceType.value == 'service' >
        Use this service
      <#else >
        Get the data
      </#if>
    </p></div>
    <div class="panel-body">
    <#if resourceType.value == 'signpost' >
      <div class="distribution-signpost">
        <#include "_signpost.ftl">
        <#include "_formats.ftl">
      </div>
    <#elseif resourceType.value == 'service' && mapViewable >
      <div class="distribution-service">
        <p><a href="${mapViewerUrl?html}"><i class="far fa-map text-info"></i> Open in map viewer</a></p>
        <#if mapservices??>
        <#list mapservices as wms>
          <p><i class="fas fa-link"></i> URL: <span class="wmsurl">${wms.url?html}</span></p>
        </#list>
        </#if>
      </div>
    <#else >
      <div class="distribution-dataset">
        <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn" >
          <p><b>THIS ${resourceType.value?upper_case} HAS BEEN WITHDRAWN</b>
          <#if revised??>
            and superseded by <a href="${revised.href}">${revised.title}</a>
          </#if>
          </p>
          <#if reasonChanged??>
            <p class="reasonChangedDescription">${reasonChanged?html?replace("\n", "<br>")}</p>
          </#if>
          <p>If you need access to the archived version, please <a href="http://eidc.ceh.ac.uk/contact" target="_blank" rel="noopener noreferrer">contact the EIDC</a></p>
        <#elseif resourceStatus == "Current" || resourceStatus == "Retired" >
          <#if downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || mapViewable || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) >
            <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
              <#list downloadOrder.orderResources as onlineResource>
                <p><a href="${onlineResource.url?html}"><i class="fas fa-download text-info"></i>
                  <#if onlineResource.name?has_content>
                    ${onlineResource.name?html}
                  <#else>
                    Order/download
                  </#if>
                  </a></p>
              </#list>
            </#if>
            <#if (downloadOrder.supportingDocumentsUrl)?has_content>
              <p><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="far fa-copy text-info"></i> Supporting documentation</a></p>
            </#if>
            <#if mapViewable>
              <p><a href="${mapViewerUrl?html}"><i class="far fa-map text-info"></i> Preview on map</a></p>
            </#if>
          </#if>
        </#if>
        <#include "_formats.ftl">
      </div>
    </#if>
    </div>
  </div>
</#if>