
<#if downloadOrderDetails??>
  <#assign dataAccess=downloadOrderDetails.from(onlineResources) >
</#if>


<div class="accessButtons"><#t/>
  <@exploreButton />
  <@accessButton />
  <@documentsButton />
  <@citeButton />
<#t/></div>

<@citationModal />
<@offcanvasAccess />

<#macro accessButton>
  <#if resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'model' || resourceType.value == 'software' || resourceType.value == 'computationalNotebook' || resourceType.value == 'method'>
    <#if availability == "Available" || availability == "Controlled" >
      <#if dataAccess?? && dataAccess.dataAccessible >
        <a class="btn btn-ukceh" data-bs-toggle="offcanvas" href="#access" role="button" aria-controls="access">
          Download
          <i class="fa-solid fa-arrow-down fa-fw"></i>
        </a>
      <#elseif externalLinks?? && externalLinks?has_content>
        <a class="btn btn-ukceh" data-bs-toggle="offcanvas" href="#access" role="button" aria-controls="access">
          Access <i class="fa-solid fa-arrow-up-right-from-square"></i>
        </a>
      </#if>
    </#if>
  <#elseif resourceType.value == 'service' && mapViewable >
    <a class="btn btn-ukceh" data-bs-toggle="offcanvas" href="#access" role="button" aria-controls="access">
      Access <i class="fa-solid fa-arrows-rotate fa-fw"></i>
    </a>
  </#if>
</#macro>

<#macro documentsButton>
  <#if (dataAccess.supportingDocumentsUrl)?has_content>
    <a class="btn btn-ukceh" href="${dataAccess.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset">
      Supporting docs
      <i class="fa-solid fa-file fa-fw"></i></span>
    </a>
  </#if>
</#macro>

<#macro exploreButton>
  <#if apps?? && apps?size gte 1>
    <#list apps as app>
      <a class="btn btn-ukceh" href="${app.url}" alt="${app.name}" target="_blank" rel="noopener noreferrer">
        ${app.name}
        <span class="fa-solid fa-laptop"></span>
      </a>
    </#list>
  </#if>
</#macro>

<#macro citeButton>
 <#if citation?has_content>
    <a href="#" class="btn btn-ukceh" data-bs-toggle="modal" data-bs-target="#citationModal">
      Cite this ${recordType?lower_case!''}
      <small><i class="fa-solid fa-quote-left fa-2xs"></i> <i class="fa-solid fa-quote-right fa-2xs"></i></small>
    </a>
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
      <#if metadata.catalogue == "eidc" && (resourceType.value == 'model' || resourceType.value == 'software' || resourceType.value == 'computationalNotebook' || resourceType.value == 'method')>
        <#assign constraints += "<p class='constraints'>Providing access to this " + recordType?lower_case + " does not signify any endorsement or approval. By accessing and using the resource, you acknowledge that it is entirely at your own risk and you are solely responsible for any loss or liability that may arise</p>" />
      </#if>

      <#if availability == "Available" || availability == "Controlled">

        <#include "_licence.ftlh">
        <div>
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

        <#if (dataAccess?? && dataAccess.dataAccessResources?has_content) || (resourceType.value?? && distributionFormats?? && distributionFormats?has_content)>
          <div class="accessOptions">
            <@accessOptions dataAccess.dataAccessResources/>

            <#if mapViewable>
              <#if mapservices?? && mapservices?has_content>
                <#list mapservices as wms>
                  <@accessOption "${wms.url}" " Web map service" "Access a web map service (WMS) for use in a GIS application" "fa-globe" "Access"/>
                </#list>
              </#if>
            </#if>
          </div>

          <@fileAccessInstructions />
        <#elseif externalLinks?? && externalLinks?has_content>
          <#list externalLinks as item>
            <p>
              <a href="${item.url}" class="link-offset-2" target="_blank" rel="noopener noreferrer">
                <b>Access</b>
                <span class="ms-3 fw-light">(${item.url})</span>
              </a>
            </p>
          </#list>
        </#if>

      </#if>

      <#include "_status.ftlh">

    </div>
  </div>
</#macro>

<#macro accessOptions resources>
    <#list resources as item>
        <#assign optionDescription = "">
        <#if item.name?has_content>
          <#assign optionLabel = item.name>
        </#if>

        <#if catalogue.id == 'eidc'>
          <#if item.function == "download" && item.url?matches("^https://data-package.ceh.ac.uk/data/.+")>
            <#assign optionLabel = "Download the whole dataset">
            <#assign optionDescription = "Download the whole dataset as a zip package">
          <#elseif item.function == "order" && item.url?matches("^https://order-eidc.ceh.ac.uk/resources/.+")>
            <#assign optionLabel = "Order the data">
            <#assign optionDescription = "Select all or part of the data and receive by email">
            <#assign optionIcon = "fa-arrow-right-from-bracket">
          <#elseif item.function == "fileAccess" && item.url?matches("^https://catalogue.ceh.ac.uk/datastore/eidchub/.+")>
            <#assign optionLabel = "Directly access files">
            <#assign optionDescription = "Access individual files ">
            <#assign optionIcon = "fa-folder-open">
          </#if>
        </#if>

        <#if item.size?has_content>
          <#if item.function == "download">
            <#assign optionDescription += " (" + calculateDownloadSize(item.size) + ")">
          <#elseif item.function == "fileAccess">
            <#assign optionDescription += " (total size " + calculateDownloadSize(item.size) + ")">
          </#if>
        </#if>

        <@accessOption "/download/${id}?url=${item.url}" optionLabel optionDescription optionIcon/>

    </#list>
</#macro>

<#macro accessOption link="#" title="Order/download" description="" icon="fa-download" buttonLabel="Download">
    <a class="access"  href="${link}">
      <div class="fa-solid fa-xl ${icon}"></div>
      <div class="accessText">
        <b>${title}</b>
        <#if description?? && description?has_content>
        <br><small class="text-body-secondary">${description}</small>
        </#if>
      </div>
      <span class="btn btn-ukceh text-nowrap">${buttonLabel}</span>
    </a>
</#macro>

<#macro fileAccessInstructions>
  <#if dataAccess?? && dataAccess.dataAddressable>
    <div class="py-3">
      <hr class="my-5 ">
      <h5 class="my-3">Bulk download options</h5>
      <p>You can use WGET to download data. For example<br>
        <code>
          wget https://catalogue.ceh.ac.uk/datastore/eidchub/${id}
          --user=YOUR_USERNAME --password=YOUR_PASSWORD --auth-no-challenge
        <#t></code>
      </p>
      <p><a href="https://eidc.ac.uk/help/getdata/downloadData#programmaticAccess" rel="nofollow noreferrer" target="_blank">Further information and help &raquo;</a></p>
    </div>
  </#if>
</#macro>

<#function calculateDownloadSize size>
    <#if size?number gt 1073741824>
      <#local sizeLabel = 'approx. ' + (size?number / 1073741824)?string["0.#"]  + 'Gb'>
    <#elseif size?number gt 10485760>
      <#local sizeLabel = 'approx. ' + (size?number / 1048576)?string["0.#"]  + 'Mb'>
    <#elseif size?number lte 10485760 && size?number gt 5242880>
      <#local sizeLabel = '< 10 Mb'>
    <#elseif size?number lte 5242880 && size?number gt 1048576>
      <#local sizeLabel = '< 5 Mb'>
    <#else>
      <#local sizeLabel = '< 1 Mb'>
    </#if>

    <#return sizeLabel>
</#function>
