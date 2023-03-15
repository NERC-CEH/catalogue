<#macro accessButton address label btnclass="btn-default" icon="fa-angles-down">
  <a href="${address}" class="active btn btn-block ${btnclass}" role="button">
    <span class="fa-solid ${icon} fa-inverse"></span>
    <span>${label}</span>
  </a>
</#macro>
<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
  <#assign offlineOrders=func.filter(onlineResources, "function", "offlineAccess") >
</#if>

<#if resourceStatus?? && resourceStatus?has_content>
  <div class=" hidden-print" id="document-distribution">
    
      <h2>Get the data</h2>
      <div class="distribution-dataset">

        <#include "_status.ftlh">
        <#include "_licence.ftlh">
        
        <#if resourceStatus == "Available" || resourceStatus == "Controlled">
          <#if offlineOrders?? || downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) || mapViewable?? >
          <div>
            <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
              <#list downloadOrder.orderResources as onlineResource>
                <#if onlineResource.url?starts_with("https://data-package.ceh.ac.uk/data/") && !onlineResource.url?ends_with(".zip") && (permission.userInGroup("Gast") || permission.userInGroup("CEH"))>
                  <#assign onlineResourceUrl = onlineResource.url + '.zip'>
                <#else>
                  <#assign onlineResourceUrl = onlineResource.url>                    
                </#if>
                
                <#assign onlineResourceLabel = "Order/download">
                <#if onlineResource.name?has_content>
                  <#assign onlineResourceLabel = onlineResource.name>
                </#if>

                <@accessButton onlineResourceUrl onlineResourceLabel "btn-download"/>
              </#list>
            </#if>

            <#if offlineOrders?? && offlineOrders?has_content >
                <#list offlineOrders as offlineOrder>

                  <#assign onlineResourceLabel = "Order data">
                  <#if offlineOrder.name?has_content>
                    <#assign onlineResourceLabel = offlineOrder.name>
                  </#if>

                  <@accessButton offlineOrder.url onlineResourceLabel "btn-download"/>
                 
                </#list>
            </#if>

            <#if (downloadOrder.supportingDocumentsUrl)?has_content>
              <@accessButton downloadOrder.supportingDocumentsUrl "Supporting documentation" "btn-supporting btn-sm"/>
            </#if>
      
          </div>
          </#if>
        <#else>
           <#if downloadOrder?? && downloadOrder.supportingDocumentsUrl?has_content>
              <div class="supportingDocumentLinks">
              You can find more information by downloading  
                <a href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset"> additional documentation</a> that accompanies this data
              </div>
            </#if>
        </#if>
       
        <#include "_citation.ftlh">
        <#include "_otherUseConstraints.ftlh">
      </div>
    
    </div>
  </#if>