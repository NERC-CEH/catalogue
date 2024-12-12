<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
  <#assign offlineOrders=func.filter(onlineResources, "function", "offlineAccess") >
</#if>

<div class="p-3 accessButtons">
  <a class="btn btn-download" data-bs-toggle="offcanvas" href="#access" role="button" aria-controls="access"><i class="fa-solid fa-download fa-fw"></i> Download data</a>

  <#if (downloadOrder.supportingDocumentsUrl)?has_content>
    <a class="btn btn-access" href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset"><i class="fa-solid fa-file-arrow-down fa-fw"></i></span> Supporting docs</a>
  </#if>

  <div class="dropdown">
    <button class="btn btn-access dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
      View record as
    </button>
    <ul class="dropdown-menu dropdown-menu-end">
      <li><a class="dropdown-item" href="${uri}?format=json">json</a></li>
      <#if resourceType?? && (resourceType.value == 'dataset' || resourceType.value == 'application' || resourceType.value == 'service' )>
        <#if citation??>
          <li><a class="dropdown-item" href="${uri?replace("/id/", "/documents/")}/datacite.xml">Datacite xml</a></li>
        </#if>
        <li><a class="dropdown-item" href="${uri}.xml?format=gemini">ISO 19115 (XML)</a></li>
        <li><a class="dropdown-item" href="${uri}?format=schema.org">schema.org (json-LD)</a></li>
        <li><a class="dropdown-item" href="${uri}?format=ttl">RDF (Turtle)</a></li>
      </#if>
    </ul>
  </div>

  <#if citation?has_content>
    <a href="#" class="btn btn-access" data-bs-toggle="modal" data-bs-target="#citationModal"><small><i class="fa-solid fa-quote-left fa-xs"></i> <i class="fa-solid fa-quote-right fa-xs"></i></small> Cite this ${recordType?lower_case!''}</a>
  </#if>
</div>

<#if citation?has_content>
  <div class="modal fade" tabindex="-1" role="dialog" id="citationModal">
    <div class="modal-dialog modal-lg modal-fullscreen-sm-down modal-dialog-centered modal-citation" role="document">
      <div class="modal-content">
        <div class="modal-body">
          <button type="button" class="btn-close position-relative top-0 start-100 translate-middle" data-bs-dismiss="modal" aria-label="Close"></button>
          <#include "_citation.ftlh">
          <#include "_licence.ftlh">
        </div>
      </div>
    </div>
  </div>
</#if>

<div class="offcanvas offcanvas-start offcanvas-access" tabindex="-1" id="access" aria-labelledby="accessLabel">
  <div class="offcanvas-header">
    <h5 class="offcanvas-title" id="accessLabel">Download</h5>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body">

    <#if resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Embargoed">
      <div class="my-3">By accessing or using this dataset, you agree to the terms of the relevant licence agreement(s). You will ensure that this dataset is cited in any publication that describes research in which the data have been used.</div>
      <#include "_licence.ftlh">
    </#if>

    <#if resourceType.value=="application" && metadata.catalogue == "eidc">
      <div class="alert alert-dark">
        <strong>PLEASE NOTE:</strong> Publication of this ${recordType?lower_case!''} by the EIDC does not signify any endorsement or approval. By accessing and using the resource, you acknowledge that it is entirely at your own risk and you are solely responsible for any loss or liability that may arise.
      </div>
    </#if>


          <#if resourceStatus == "Available" || resourceStatus == "Controlled" || resourceStatus == "Superseded">
            <#if offlineOrders?? || downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) || mapViewable?? >
              <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
                <#list downloadOrder.orderResources as onlineResource>

                  <#assign onlineResourceUrl = onlineResource.url>
                  <#if onlineResource.url?starts_with("https://data-package.ceh.ac.uk/data/") && !onlineResource.url?ends_with(".zip") && (permission.userInGroup("Gast") || permission.userInGroup("CEH"))>
                    <#assign onlineResourceUrl = onlineResourceUrl + '.zip'>
                  </#if>

                  <#assign onlineResourceLabel = "Order/download">
                  <#if onlineResource.name?has_content>
                    <#assign onlineResourceLabel = onlineResource.name>
                  </#if>

                  <#if onlineResource.size?has_content>
                    <#if onlineResource.size?number gt 1073741824>
                      <#assign sizeLabel = 'approx. ' + (onlineResource.size?number / 1073741824)?string["0.#"]  + 'Gb'>
                    <#elseif onlineResource.size?number gt 10485760>
                       <#assign sizeLabel = 'approx. ' + (onlineResource.size?number / 1048576)?string["0.#"]  + 'Mb'>
                    <#elseif onlineResource.size?number lte 10485760 && onlineResource.size?number gt 5242880>
                       <#assign sizeLabel = '< 10 Mb'>
                    <#elseif onlineResource.size?number lte 5242880 && onlineResource.size?number gt 1048576>
                       <#assign sizeLabel = '< 5 Mb'>
                    <#else>
                      <#assign sizeLabel = '< 1 Mb'>
                    </#if>
                  </#if>

                  <a href="/download/${id}?url=${onlineResourceUrl?url}" class="btn btn-download">
                    <span class="fa-solid fa-angles-down"></span>
                    ${onlineResourceLabel}
                    <#if sizeLabel??>
                      <small>(${sizeLabel})</small>
                    </#if>
                  </a>

                </#list>
              </#if>

              <#if offlineOrders?? && offlineOrders?has_content >
                <#list offlineOrders as offlineOrder>
                  <a class="btn btn-download" href="${offlineOrder.url}"><span class="fa-solid fa-file-signature"></span> <#if offlineOrder.description?has_content>${offlineOrder.description}<#elseif offlineOrder.name?has_content>${offlineOrder.name}<#else>Order data</#if></a>
                </#list>
              </#if>

            </#if>
          </#if>

  </div>
</div>
