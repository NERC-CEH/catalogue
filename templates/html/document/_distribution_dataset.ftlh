<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
  <#assign offlineOrders=func.filter(onlineResources, "function", "offlineAccess") >
</#if>


<#if resourceStatus?? && resourceStatus?has_content>
  <div class=" hidden-print" id="document-distribution">
    
      <p class="panel-title">Get the data</p>
      <div class="distribution-dataset">
        <#include "_status.ftlh">
        
        <#if resourceStatus == "Available" || resourceStatus == "Controlled">
          <#if offlineOrders?? || downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) || mapViewable?? >
            <div class="onlineResourceLinks">
            <p>By downloading, accessing or using this dataset, you agree to the terms of the relevant licence agreement(s)</p>
            <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
              <#list downloadOrder.orderResources as onlineResource>
                <#if onlineResource.url?starts_with("https://data-package.ceh.ac.uk/data/") && !onlineResource.url?ends_with(".zip") && (permission.userInGroup("Gast") || permission.userInGroup("CEH"))>
                  <#assign onlineResourceUrl = onlineResource.url + '.zip'>
                <#else>
                  <#assign onlineResourceUrl = onlineResource.url>                    
                </#if>
                <div class="order"><a href="${onlineResourceUrl}"><i class="fas fa-download text-info"></i>
                  <#if onlineResource.name?has_content>
                    ${onlineResource.name}
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
              <div class="information"><a href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset"><i class="fas fa-file text-info"></i> Supporting documentation</a></div>
            </#if>
      
            </div>
            <div class="divider"></div>
          </#if>
        </#if>
       
        <#if resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Embargoed">
          <#include "_licence.ftlh">
        </#if>

        <#include "_formats.ftlh">
        <#include "_citation.ftlh">
        <#include "_otherUseConstraints.ftlh">
      </div>
    
    </div>
  </#if>