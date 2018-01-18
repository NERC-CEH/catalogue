<#if resourceStatus  == "Superseded" || resourceStatus == "Withdrawn" || resourceStatus == "Current" || resourceStatus == "Retired" >

  <#if resourceType?? && resourceType.value == 'service'>
    <#assign documentOrderTitle="View the data">
    <#assign viewOnMap="Open in map viewer">
  <#else>
    <#assign documentOrderTitle="Get the data">
    <#assign viewOnMap="Preview on map">
  </#if>

  <#if onlineResources?? >
    <#assign downloadOrder=downloadOrderDetails.from(onlineResources)>
  </#if>

  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading">
      <p class="panel-title">${documentOrderTitle?html}</p>
    </div>
    <div class="panel-body">
      <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn" >
            <p><b>THIS ${resourceType.value?upper_case} HAS BEEN WITHDRAWN</b>
            <#if revised??>
              and superseded by <a href="${revised.href}">${revised.title}</a>
            </#if>
            .</p>
            <#if erratum??>
              <p class="errataDescription">${erratum?html?replace("\n", "<br>")}</p>
            </#if>
            <p>If you need access to the archived version, please <a href="#">contact EIDC</a></p>
      <#elseif resourceStatus == "Current" || resourceStatus == "Retired" >
        <#if downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || mapViewable || ((resourceType.value)?? && resourceType.value != 'service' && distributionFormats?? && distributionFormats?has_content) >
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
            <p><a href="${mapViewerUrl?html}"><i class="fa fa-map-o text-info"></i> ${viewOnMap?html}</a></p>
          </#if>
        </#if>
      <#else>
      </#if>
      <#if resourceType.value != 'service'>
        <#include "_formats.html.tpl">
      </#if>
      <#if resourceStatus = "Retired" && parent?has_content>
        <div class="alert alert-${resourceStatus}"> 
          <p>See other resources in this series: <a href="${parent.href?html}">${parent.title?html}</a></p>
        </div>
      </#if>
    </div>
  </div>
</#if>



