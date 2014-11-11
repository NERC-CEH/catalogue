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
    <div class="panel-body" property="dcat:Distribution">
      
      <#if downloadOrder.orderUrl?has_content>
        <p><a href="${downloadOrder.orderUrl}" property="dcat:accessURL"><i class="glyphicon glyphicon-save text-info"></i> Order/download</a></p>
      </#if>
      <#if mapViewable>
        <p><a href="${mapViewerUrl}"><i class="glyphicon glyphicon-map-marker text-info"></i> ${viewOnMap}</a></p>
      </#if>
      <#if downloadOrder.supportingDocumentsUrl?has_content>
        <p><a href="${downloadOrder.supportingDocumentsUrl}" title="Supporting information important for the re-use of this dataset"><i class="glyphicon glyphicon-book text-info"></i> Supporting documentation</a></p>
      </#if>

      <#if resourceType != 'service'>
        <#include "_formats.html.tpl">
      </#if>

      <#list useLimitations as useLimitations>
        <#if !useLimitations?starts_with("If you reuse this data")>
          <p property="dc:rights">${useLimitations}</p>
        </#if>
      </#list>

    </div>
    
    <div class="panel-body">
      <#include "_citation.html.tpl">
    </div>
  </div>
</#if>
