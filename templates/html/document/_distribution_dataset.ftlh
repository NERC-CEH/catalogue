<#macro makeButton link="#" btnclass="" icon="">
   <a href="${link}" class="button ${btnclass}">
      <icon class="fa-stack fa-lg">
        <i class="fa-solid fa-circle fa-stack-2x fa-inverse"></i>
        <i class="fa-solid ${icon} fa-stack-1x "></i>
      </icon>
      <span><#nested></span>
    </a>
</#macro>

     
<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
  <#assign offlineOrders=func.filter(onlineResources, "function", "offlineAccess") >
</#if>

<#if resourceStatus?? && resourceStatus?has_content>
<#assign displayDataDownload = 1>
<a name="download"></a>
  <div class=" hidden-print" id="document-distribution">
    
      <h2>Access &amp; licensing</h2>
      <div class="distribution-dataset">


        <div class="statusMessage">
          <#include "_status.ftlh">
        </div>

        <#if resourceStatus == "Available" || resourceStatus == "Controlled">
          <#if offlineOrders?? || downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) || mapViewable?? >
            <div class="accessButtons">

              <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
                <#list downloadOrder.orderResources as onlineResource>
                  <#if onlineResource.url?starts_with("https://data-package.ceh.ac.uk/data/") && !onlineResource.url?ends_with(".zip") && (permission.userInGroup("Gast") || permission.userInGroup("CEH"))>
                    <#assign onlineResourceUrl = onlineResource.url + '.zip'>
                  <#else>
                    <#assign onlineResourceUrl = onlineResource.url>                    
                  </#if>

                  <@makeButton "${onlineResourceUrl}" "button-download" "fa-arrow-down">
                    <#if onlineResource.name?has_content>
                      ${onlineResource.name}
                    <#else>
                      Order/download
                    </#if>
                  </@makeButton>
                  
                </#list>
              </#if>

              <#if offlineOrders?? && offlineOrders?has_content >
                  <#list offlineOrders as offlineOrder>
                    <@makeButton "${offlineOrder.url}" "button-download" "fa-file-signature">
                      <#if offlineOrder.description?has_content>${offlineOrder.description}<#elseif offlineOrder.name?has_content>${offlineOrder.name}<#else>Order data</#if>
                    </@makeButton>
                  </#list>
              </#if>

              <#if (downloadOrder.supportingDocumentsUrl)?has_content>
                <@makeButton "${downloadOrder.supportingDocumentsUrl}" "button-supporting" "fa-file-lines">
                  Supporting documentation
                </@makeButton>
              </#if>
            </div>

            <#if resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Embargoed">
              <p class="licencePreamble">By accessing or using this dataset, you agree to the terms of the relevant licence agreement(s). You will ensure that this dataset is cited in any publication that describes research in which the data have been used.</p>
              <#include "_licence.ftlh">
            </#if>
        </#if>
      </#if>
       
        <#include "_citation.ftlh">
        <#include "_otherUseConstraints.ftlh">
      </div>
    
    </div>
  </#if>