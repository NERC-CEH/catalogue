<#if downloadOrder?has_content>
<div id="document-ordering" class="panel panel-default">
  <div class="panel-heading"><p class="panel-title">Get the data</p></div>
    <div id="download-link" class="panel-body">
      <#if downloadOrder.orderUrl?has_content>
        <p resource="${downloadOrder.orderUrl}" property="dcat:accessURL">
          <a href="${downloadOrder.orderUrl}">Order/Download</a>
        </p>
      <#else>
        <p property="dcat:accessURL">
          No Order/Download available
        </p>
      </#if>
      <#if downloadOrder.supportingDocumentsUrl?has_content>
        <p>
          <a href="${downloadOrder.supportingDocumentsUrl}" rel="help">Supporting documentation</a>
        </p>
      </#if>
    </div>
    <#if downloadOrder.licenseUrl?has_content>
    <div id="license" class="panel-footer column-sm">
      <p resource="${downloadOrder.licenseUrl}" property="dc:rights">
      <#if downloadOrder.isOgl()>
        <a href="${downloadOrder.licenseUrl}" rel="license">
          <img src="/static/img/ogl.png" alt="OGL" class="img-responsive pull-right">This resource is available under the Open Government Licence (OGL)
        </a>
      <#else>
        <a href="${downloadOrder.licenseUrl}" rel="license">Licence terms and conditions apply</a>
      </#if>
      </p>
    </div>
    </#if>
</div>
</#if>
