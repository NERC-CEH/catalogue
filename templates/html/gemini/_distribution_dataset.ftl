<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
</#if>

  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading">
      <p class="panel-title">Get the data</p>
    </div>
    <div class="panel-body">
      <div class="distribution-dataset">
        <#if resourceStatus == "Sperseded" || resourceStatus == "Withdrawn" >
          <p><b>THIS ${resourceType.value?upper_case} HAS BEEN WITHDRAWN</b>
          <#if revised??>
            and superseded by <a href="${revised.href}">${revised.title}</a>
          </#if>
          </p>
          <#if reasonChanged??>
            <p class="reasonChangedDescription">${reasonChanged?html?replace("\n", "<br>")}</p>
          </#if>
          <p>If you need access to the archived version, please <a href="http://eidc.ceh.ac.uk/contact" target="_blank" rel="noopener noreferrer">contact the EIDC</a></p>
        <#elseif resourceStatus == "available" || resourceStatus == "retired" >
          <#if downloadOrder?? && ((downloadOrder.orderResources)?has_content || (downloadOrder.supportingDocumentsUrl)?has_content) || mapViewable || ((resourceType.value)?? && distributionFormats?? && distributionFormats?has_content) >
            <#if (downloadOrder.orderable)?? && downloadOrder.orderable>
              <#list downloadOrder.orderResources as onlineResource>
                <p><a href="${onlineResource.url?html}"><i class="fas fa-download text-info"></i>
                  <#if onlineResource.name?has_content>
                    ${onlineResource.name?html}
                  <#else>
                    Order/download
                  </#if>
                  </a></p>
              </#list>
            </#if>
            <#if (downloadOrder.supportingDocumentsUrl)?has_content>
              <p><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="far fa-copy text-info"></i> Supporting documentation</a></p>
            </#if>
            <#if mapViewable>
              <p><a href="${mapViewerUrl?html}"><i class="far fa-map text-info"></i> Preview on map</a></p>
            </#if>
          </#if>
        </#if>
        <#include "_formats.ftl">
      </div>
    </div>
  </div>