<#if onlineResources?? >
  <#assign downloadOrder=downloadOrderDetails.from(onlineResources) >
</#if>
<#if resourceStatus??>
  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading">
      <p class="panel-title">Get the data</p>
    </div>
    <div class="panel-body">
      <div class="distribution-dataset">
        <#include "_status.ftl">
        <#if resourceStatus == "Available" >
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
              <p><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="fas fa-file text-info"></i> Supporting documentation</a></p>
            </#if>
            <#if mapViewable>
              <p><a href="${mapViewerUrl?html}"><i class="fas fa-eye text-info"></i> Preview</a></p>
            </#if>
          </#if>
        </#if>
        <#include "_formats.ftl">
        <#include "_licence.ftl">
        <#include "_citation.ftl">
        <#include "_otherUseConstraints.ftl">
      </div>
    </div>
  </div>
  </#if>