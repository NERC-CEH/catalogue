<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
  <#assign offlineOrders=func.filter(onlineResources, "function", "offlineAccess") >
</#if>


<#if resourceStatus?? && resourceStatus?has_content>
  <div class=" hidden-print" id="document-distribution">
    
      <h2>Get the data</h2>
      <div class="distribution-dataset">

        <#include "_status.ftlh">

        <#if resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Embargoed">
          <p class="licencePreamble">By accessing or using this dataset, you agree to the terms of the relevant licence agreement(s). You will ensure that this dataset is cited in any publication that describes research in which the data have been used.</p>
          <#include "_licence.ftlh">
        </#if>
        
        
        <#if resourceStatus == "Available" || resourceStatus == "Controlled">
          <#if offlineOrders?? || downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) || mapViewable?? >
            <div class="onlineResourceLinks">
            <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
              <#list downloadOrder.orderResources as onlineResource>
                <#if onlineResource.url?starts_with("https://data-package.ceh.ac.uk/data/") && !onlineResource.url?ends_with(".zip") && (permission.userInGroup("Gast") || permission.userInGroup("CEH"))>
                  <#assign onlineResourceUrl = onlineResource.url + '.zip'>
                <#else>
                  <#assign onlineResourceUrl = onlineResource.url>                    
                </#if>
                <a href="${onlineResourceUrl}"><i class="fas fa-download text-info"></i>
                  <#if onlineResource.name?has_content>
                    ${onlineResource.name}
                  <#else>
                    Order/download
                  </#if>
                  </a>
              </#list>
            </#if>

            <#if offlineOrders?? && offlineOrders?has_content >
                <#list offlineOrders as offlineOrder>
                  <a href="${offlineOrder.url}"><i class="fas fa-file-signature"></i> <#if offlineOrder.description?has_content>${offlineOrder.description}<#elseif offlineOrder.name?has_content>${offlineOrder.name}<#else>Order data</#if></a>
                </#list>
            </#if>

            <#if (downloadOrder.supportingDocumentsUrl)?has_content>
              <a href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset"><i class="fas fa-file text-info"></i> Supporting documentation</a>
            </#if>
      
            </div>
          </#if>
        </#if>
       
        <#include "_citation.ftlh">
        <#include "_otherUseConstraints.ftlh">
      </div>
    
    </div>
  </#if>