<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
  <#assign offlineOrders=func.filter(onlineResources, "function", "offlineAccess") >
</#if>


<#if resourceStatus??>
  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading">
      <p class="panel-title">Get the data</p>
    </div>
    <div class="panel-body">
      <div class="distribution-dataset">
        <#include "_status.ftl">
        <#include "_licence.ftl">
        <#if resourceStatus == "Available" || resourceStatus == "Controlled">
          <#if offlineOrders?? || downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) || mapViewable?? >
            <div class="onlineResourceLinks">
            <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
              <#list downloadOrder.orderResources as onlineResource>
                <#if onlineResource.url?starts_with("https://data-package.ceh.ac.uk/data/") && (permission.userInGroup("Gast") || permission.userInGroup("CEH"))>
                  <#assign onlineResourceUrl = onlineResource.url + '.zip'>
                <#else>
                  <#assign onlineResourceUrl = onlineResource.url>                    
                </#if>
                <div class="order"><a href="${onlineResourceUrl}"><i class="fas fa-download text-info"></i>
                  <#if onlineResource.name?has_content>
                    ${onlineResource.name?html}
                  <#else>
                    Order/download
                  </#if>
                  </a></div>
              </#list>
            </#if>

            <#if offlineOrders?? && offlineOrders?has_content >
              <div class="offlineDataRequest">
                <#list offlineOrders as offlineOrder>
                <div class="offlineAccess"><a href="${offlineOrder.url}">
                  <i class="fas fa-file-signature"></i> <#if offlineOrder.description?has_content>${offlineOrder.description}<#elseif offlineOrder.name?has_content>${offlineOrder.name}<#else>Order data</#if></a></div>
                </#list>
              </div>
            </#if>

            <#if (downloadOrder.supportingDocumentsUrl)?has_content>
              <div class="information"><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="fas fa-file text-info"></i> Supporting documentation</a></div>
            </#if>

            <#if mapViewable>
              <div class="preview"><a href="${mapViewerUrl?html}"><i class="fas fa-eye text-info"></i> Preview</a></div>
            </#if>
            </div>
          </#if>

        <div class="divider"></div>
        </#if>
        <#include "_formats.ftl">
        <#include "_citation.ftl">
        <#include "_otherUseConstraints.ftl">
      </div>
    </div>
  </div>
  </#if>