<#if resourceType?? && resourceType.value == 'service'>
  <#assign documentOrderTitle="View the data">
  <#assign viewOnMap="Open in map viewer">
<#else>
  <#assign documentOrderTitle="Get the data">
  <#assign viewOnMap="Preview on map">
</#if>

<#if downloadOrder?? || mapViewable >
  <div class="panel panel-default hidden-print" id="document-order">
    <div class="panel-heading"><p class="panel-title">${documentOrderTitle?html}</p></div>
    <div class="panel-body">
	
	  <#if (downloadOrder.orderUrl)?has_content>
		  <p><a href="${downloadOrder.orderUrl?html}"><i class="glyphicon glyphicon-save text-info"></i> Order/download</a></p>
	  </#if>
		
      <#if (downloadOrder.supportingDocumentsUrl)?has_content>
        <p><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="glyphicon glyphicon-file text-info"></i> Supporting documentation</a></p>
      </#if>
	  
      <#if mapViewable>
        <p><a href="${mapViewerUrl?html}"><i class="glyphicon glyphicon-map-marker text-info"></i> ${viewOnMap?html}</a></p>
      </#if>

	  
      <#if resourceType.value != 'service'>
          <#include "_formats.html.tpl">
      </#if>

	<hr>
	<#include "_limitations.html.tpl">
      
    </div>
  </div>
</#if>
