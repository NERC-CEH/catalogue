<#assign displayButtonRow = false>
<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
</#if>
<#if (resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'application') && (resourceStatus == "Available" || resourceStatus == "Controlled") && (downloadOrder?? && downloadOrder?string?contains("isOrderable=true")) || (resourceType.value == 'service' && mapViewable ) || downloadOrder.supportingDocumentsUrl?has_content>
  <#assign displayButtonRow = true>
</#if>

<#if displayButtonRow>
  <div class="py-3 d-flex flex-row align-items-center flex-wrap accessButtons d-print-none border-bottom border-1">

    <@exploreButton />

    <#if (resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'application') && (resourceStatus == "Available" || resourceStatus == "Controlled") >
      <#if downloadOrder?? && downloadOrder?string?contains("isOrderable=true") >
        <a class="btn btn-download" data-bs-toggle="offcanvas" href="#access" role="button" aria-controls="access"><i class="fa-solid fa-angles-down fa-fw"></i> Download data</a>
      </#if>
    </#if>

    <#if resourceType.value == 'service' && mapViewable >
      <a class="btn btn-download" data-bs-toggle="offcanvas" href="#access" role="button" aria-controls="access"><i class="fa-solid fa-arrows-rotate fa-fw"></i> Access</a>
    </#if>

    <#if (downloadOrder.supportingDocumentsUrl)?has_content>
      <a class="btn btn-access" href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset"><i class="fa-solid fa-file-arrow-down fa-fw"></i></span> Supporting docs</a>
    </#if>

    <@citeButton />
  </div>
</#if>

<@citationModal />
<@offcanvasAccess />

<#macro downloadButton onlineResource>
  <#assign onlineResourceUrl = onlineResource.url>

    <#if onlineResourceUrl?matches(downloadUrlRegexes.getRegexDatastore(), "i")>
      <#assign downloadType="direct"/>
    <#elseif onlineResourceUrl?matches(downloadUrlRegexes.getRegexPackage(), "i")>
      <#assign downloadType="package"/>
      <#assign onlineResourceUrl = onlineResourceUrl + '.zip'>
    <#elseif onlineResourceUrl?matches(downloadUrlRegexes.getRegexOrder(), "i")>
      <#assign downloadType="order"/>
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

    <a href="/download/${id}?url=${onlineResourceUrl?url}" class="btn btn-primary">
      <span class="fa-solid fa-angles-down"></span>
      ${onlineResourceLabel}
      <#if sizeLabel??>
        <small>(${sizeLabel})</small>
      </#if>
    </a>

    <#if downloadType?? && downloadType=="direct">
      <div class="py-3">
        <hr>
        <h5 class="my-3">Bulk download options</h5>
        <p>You can use WGET to download data. For example<br>
          <code> wget --user=YOUR_USERNAME --password=YOUR_PASSWORD --auth-no-challenge ${onlineResourceUrl}</code>
        </p>
        <p><a href="https://eidc.ac.uk/help/getdata/downloadData#programmaticAccess" rel="nofollow noreferrer" target="_blank">Further information and help &raquo;</a></p>
      </div>
    </#if>

</#macro>

<#macro exploreButton>
  <#if browsers?? && browsers?size gte 1>
    <#list browsers>
      <#items as browser>
          <a class="btn btn-explore" href="${browser.url}" alt="${browser.name}">
           <span class="fa-solid fa-laptop"></span> ${browser.name}</a>
      </#items>
    </#list>
  </#if>
</#macro>

<#macro citeButton>
 <#if citation?has_content>
    <a href="#" class="btn btn-access" data-bs-toggle="modal" data-bs-target="#citationModal"><small><i class="fa-solid fa-quote-left fa-2xs"></i> <i class="fa-solid fa-quote-right fa-2xs"></i></small> Cite this ${recordType?lower_case!''}</a>
  </#if>
</#macro>

<#macro citationModal>
  <#if citation?has_content>
    <div class="modal fade" tabindex="-1" role="dialog" id="citationModal">
      <div class="modal-dialog modal-lg modal-fullscreen-sm-down modal-dialog-centered modal-citation" role="document">
        <div class="modal-content">
          <div class="modal-body">
            <button type="button" class="btn-close position-relative top-0 start-100 translate-middle" data-bs-dismiss="modal" aria-label="Close"></button>
            <#include "_citation.ftlh">
            <div class="mt-3"><#include "_licence.ftlh"></div>
          </div>
        </div>
      </div>
    </div>
  </#if>
</#macro>

<#macro offcanvasAccess title="Download/Access">
  <div class="offcanvas offcanvas-start offcanvas-access" tabindex="-1" id="access" aria-labelledby="accessLabel">
    <div class="offcanvas-header">
      <h5 class="offcanvas-title" id="accessLabel">${title}</h5>
      <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body">

      <#assign constraints = "<p class='constraints'>By accessing or using this " + recordType?lower_case + ", you agree to the terms of the relevant licence agreement(s). You will ensure that this " + recordType?lower_case + " is cited in any publication that describes research in which the data have been used.</p>" />
      <#if metadata.catalogue == "eidc" && resourceType.value=="application">
        <#assign constraints += "<p class='constraints'>Publication of this " + recordType?lower_case + " by the EIDC does not signify any endorsement or approval. By accessing and using the resource, you acknowledge that it is entirely at your own risk and you are solely responsible for any loss or liability that may arise</p>" />
      </#if>

      <#if resourceStatus == "Available" || resourceStatus == "Controlled">

        <div class="card text-bg-primary mb-3">
          <div class="card-body">
            <strong>PLEASE NOTE:</strong>
            <hr>
            ${constraints?no_esc}

            <#if otherConstraints?has_content>
              <#list otherConstraints as oc>

                <#assign otherConstraintText = oc.value >

                <#if recordType?? && recordType?has_content>
                  <#assign otherConstraintText = otherConstraintText?replace("resource",recordType) >
                </#if>

                <#if oc.uri?has_content>
                  <#assign otherConstraintText = "<a href='${oc.uri}'>"+ otherConstraintText + '</a>'>
                </#if>

                <p class="constraints">${otherConstraintText}</p>
              </#list>
            </#if>
          </div>
        </div>

        <#include "_licence.ftlh">

        <#if downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content)>
          <#list downloadOrder.orderResources as item>
            <@downloadButton item/>
          </#list>
        </#if>

      </#if>

      <#include "_status.ftlh">

      <#if mapViewable?? >

        <#if onlineResources?? >
          <#assign mapservices = func.filterRegex(onlineResources, "url", "https://catalogue.ceh.ac.uk/maps/")>
        </#if>

        <#if mapservices?? && mapservices?has_content>
        <div class="mb-1">Add to your GIS application:</div>
        <div class="ms-3">
          <#list mapservices as wms>
            ${wms.url}<#sep><br></#sep>
          </#list>
        </div>
        </#if>

      </#if>

    </div>
  </div>
</#macro>
