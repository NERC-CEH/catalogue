<#if resourceType == 'service'>
  <#assign documentOrderTitle="View the data">
  <#assign viewOnMap="Open in map viewer">
<#else>
  <#assign documentOrderTitle="Get the data">
  <#assign viewOnMap="Preview on map">
</#if>


<#if downloadOrder.orderUrl?has_content||mapViewable >
  <div class="panel panel-default hidden-print" id="document-order">
    <div class="panel-heading"><p class="panel-title">${documentOrderTitle?html}</p></div>
    <div class="panel-body">
      <#if mapViewable>
        <p><a href="${mapViewerUrl?html}"><i class="glyphicon glyphicon-map-marker text-info"></i> ${viewOnMap?html}</a></p>
      </#if>
      <#if downloadOrder.supportingDocumentsUrl?has_content>
        <p><a href="${downloadOrder.supportingDocumentsUrl?html}" title="Supporting information important for the re-use of this dataset"><i class="glyphicon glyphicon-book text-info"></i> Supporting documentation</a></p>
      </#if>

      <div rel="dcat:Distribution">
        <#if downloadOrder.orderUrl?has_content>
          <p><a href="${downloadOrder.orderUrl?html}" property="dcat:accessURL"><i class="glyphicon glyphicon-save text-info"></i> Order/download</a></p>
        </#if>

        <#if resourceType != 'service'>
          <#include "_formats.html.tpl">
        </#if>

        <#list useLimitations as useLimitations>
          <#-- starts_with shortened to catch 'reuse' and 're-use' in the wild -->
          <#if !useLimitations?starts_with("If you re")>
            <p property="dc:rights">${useLimitations?html}</p>
          </#if>
        </#list>
      </div>
      <#include "_citation.html.tpl">
    </div>
  </div>
</#if>
