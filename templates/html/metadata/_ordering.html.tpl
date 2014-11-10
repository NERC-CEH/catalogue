<#if resourceType == 'service'>
  <#assign documentOrderTitle="View the data">
  <#assign viewOnMap="Open in map viewer">
<#else>
  <#assign documentOrderTitle="Get the data">
  <#assign viewOnMap="Preview on map">
</#if>


<#if downloadOrder.orderUrl?has_content||mapViewable >
  <div class="panel panel-default hidden-print" id="document-order">
    <div class="panel-heading"><p class="panel-title">${documentOrderTitle}</p></div>
    <div class="panel-body">
      
      <#if downloadOrder.orderUrl?has_content>
        <p><a href="#" property="dcat:accessURL"><i class="glyphicon glyphicon-save text-info"></i> Order/download</a></p>
      </#if>
      <#if mapViewable>
        <p><a href="${mapViewerUrl}"><i class="glyphicon glyphicon-map-marker text-info"></i> ${viewOnMap}</a></p>
      </#if>
      <#if downloadOrder.supportingDocumentsUrl?has_content>
        <p><a href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset"><i class="glyphicon glyphicon-book text-info"></i> Supporting documentation</a></p>
      </#if>
    </div>
    
    <#if resourceType != 'service'>
      <div class="panel-body" id="section-formats">
        <#include "_formats.html.tpl">
      </div>
    </#if>
    
    <div class="panel-body" id="section-licence">
      <#include "_accessInfo.html.tpl">
    </div>
  </div>
</#if>
