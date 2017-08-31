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

<#if downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || mapViewable || ((resourceType.value)?? && resourceType.value != 'service' &&distributionFormats?? && distributionFormats?has_content) >
  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading"><p class="panel-title">${documentOrderTitle?html}</p></div>
    <div class="panel-body">
      <div>
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
      <#else>
        <#if (downloadOrder.orderResources)??>
          <#list downloadOrder.orderResources as onlineResource>
            <div class="alert alert-warning">
              <p>${onlineResource.description?html}</p>
              <#if (onlineResource.url)?has_content>
                <p><a href="${onlineResource.url?html}">More Information</a></p>
              </#if>
            </div>
          </#list>
        </#if>
      </#if>

      <#if (downloadOrder.supportingDocumentsUrl)?has_content>
        <p><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="fa fa-files-o text-info"></i> Supporting documentation</a></p>
      </#if>

      <#if mapViewable>
        <p><a href="${mapViewerUrl?html}"><i class="fa fa-map-o text-info"></i> ${viewOnMap?html}</a></p>
      </#if>
    </div>

    <#if resourceType.value != 'service'>
      <#include "_formats.html.tpl">
    </#if>
  </div>
  </div>
</#if>