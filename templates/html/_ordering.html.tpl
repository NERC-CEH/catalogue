<#if downloadOrder?has_content>
<div class="panel panel-default document-ordering">
  <div class="panel-heading"><p class="panel-title">Get the data</p></div>
    <div class="panel-body">
      <#if downloadOrder.orderUrl?has_content>
        <p>
          <a href="${downloadOrder.orderUrl}">Order/Download</a>
        </p>
      <#else>
        <p>
          No Order/Download available
        </p>
      </#if>
      <#if downloadOrder.supportingDocumentsUrl?has_content>
        <p>
          <a href="${downloadOrder.supportingDocumentsUrl}">Supporting documentation</a>
        </p>
      </#if>
    </div>
    <div class="panel-footer column-sm">
    <#if downloadOrder.licenseUrl?has_content>
      <#if downloadOrder.isOgl()>
        <p><a href="${downloadOrder.licenseUrl}"><img src="/static/img/ogl.png" alt="OGL" class="img-responsive pull-right">This resource is available under the Open Government Licence (OGL)</a></p>
      <#else>
  	     <a href="${downloadOrder.licenseUrl}">Licence terms and conditions apply</a>
      </#if>
    </#if>
    </div>
</div>
</#if>
