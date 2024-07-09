<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
  <#assign offlineOrders=func.filter(onlineResources, "function", "offlineAccess") >
</#if>


<#if resourceStatus?? && resourceStatus?has_content>
  <div class=" hidden-print" id="document-distribution">

      <h2>Get the data</h2>
      <div class="distribution-dataset">

        <div class="statusMessage">
          <#include "_status.ftlh">
        </div>

        <#if resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Embargoed">
          <p class="licencePreamble">By accessing or using this dataset, you agree to the terms of the relevant licence agreement(s). You will ensure that this dataset is cited in any publication that describes research in which the data have been used.</p>
          <#include "_licence.ftlh">
        </#if>


        <div class="onlineResourceLinks">
          <#if resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Superseded">
            <#if offlineOrders?? || downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) || mapViewable?? >
              <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
                <#list downloadOrder.orderResources as onlineResource>
                  <#if onlineResource.url?starts_with("https://data-package.ceh.ac.uk/data/") && !onlineResource.url?ends_with(".zip") && (permission.userInGroup("Gast") || permission.userInGroup("CEH"))>
                    <#assign onlineResourceUrl = onlineResource.url + '.zip'>
                  <#else>
                    <#assign onlineResourceUrl = onlineResource.url>
                  </#if>
                  <a href="/download/${id}?url=${onlineResourceUrl?url}" class="btn btn-download"><span class="fa-solid fa-angles-down"></span>
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
                    <a  class="btn btn-download" href="${offlineOrder.url}"><span class="fa-solid fa-file-signature"></span> <#if offlineOrder.description?has_content>${offlineOrder.description}<#elseif offlineOrder.name?has_content>${offlineOrder.name}<#else>Order data</#if></a>
                  </#list>
              </#if>

            </#if>
          </#if>

          <#if (downloadOrder.supportingDocumentsUrl)?has_content>
            <a href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset" class="btn btn-supporting"><span class="fa-solid fa-angles-down"></span> Supporting documentation</a>
          </#if>
        </div>

        <#include "_citation.ftlh">
        <#include "_otherUseConstraints.ftlh">
      </div>

    </div>
  </#if>