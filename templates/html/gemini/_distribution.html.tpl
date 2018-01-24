<#if resourceType?? && resourceType.value != 'service'>
  <#if resourceStatus?? && (resourceStatus == "Superseded" || resourceStatus == "Withdrawn" || resourceStatus == "Current" || resourceStatus == "Retired") >

    <#if onlineResources?? >
      <#assign downloadOrder=downloadOrderDetails.from(onlineResources)>
    </#if>

    <div class="panel panel-default hidden-print" id="document-distribution">
      <div class="panel-heading">
        <p class="panel-title">Get the data</p>
      </div>
      <div class="panel-body">
        <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn" >
              <p><b>THIS ${resourceType.value?upper_case} HAS BEEN WITHDRAWN</b>
              <#if revised??>
                and superseded by <a href="${revised.href}">${revised.title}</a>
              </#if>
              </p>
              <#if erratum??>
                <p class="errataDescription">${erratum?html?replace("\n", "<br>")}</p>
              </#if>
              <p>If you need access to the archived version, please <a href="#">contact EIDC</a></p>
        <#elseif resourceStatus == "Current" || resourceStatus == "Retired" >
          <#if downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || mapViewable || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) >
            <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
              <#list downloadOrder.orderResources as onlineResource>
                <p><a href="${onlineResource.url?html}"><i class="fa fa-download text-info"></i>
                  <#if onlineResource.name?has_content>
                    ${onlineResource.name?html}
                  <#else>
                    Order/download
                  </#if>
                  </a></p>
              </#list>
            </#if>
            <#if (downloadOrder.supportingDocumentsUrl)?has_content>
              <p><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="fa fa-files-o text-info"></i> Supporting documentation</a></p>
            </#if>
            <#if mapViewable>
              <p><a href="${mapViewerUrl?html}"><i class="fa fa-map-o text-info"></i> Preview on map</a></p>
            </#if>
          </#if>
        <#else>
        </#if>
        <#include "_formats.html.tpl">
        <#if resourceStatus = "Retired" && parent?has_content>
          <div class="alert alert-${resourceStatus}"> 
            <p>See other resources in this series: <a href="${parent.href?html}">${parent.title?html}</a></p>
          </div>
        </#if>
      </div>
    </div>
  </#if>
<#elseif resourceType?? && resourceType.value == 'service' && mapViewable>
    <div class="panel panel-default hidden-print" id="document-distribution">
      <div class="panel-heading">
          <p class="panel-title">View the data</p>
      </div>
      <div class="panel-body">
        <p><a href="${mapViewerUrl?html}"><i class="fa fa-map-o text-info"></i> Open in map viewer</a></p>
      </div>
    </div>
<#else>
</#if>