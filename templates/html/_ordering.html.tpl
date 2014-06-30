<#if downloadOrder?has_content>
<div class="panel panel-default">
  <div class="panel-heading"><p class="panel-title">Get the data</p></div>
    <div class="panel-body">
      <#if downloadOrder.orderUrl?has_content>
        <p>
          Preview
        </p>
      </#if>
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
    <div class="panel-footer">
    <#if downloadOrder.licenseUrl?has_content>
      <#if downloadOrder.isOgl()>
        <a href="${downloadOrder.licenseUrl}"><img src="/static/img/ogl.png" alt="OGL" class="pull-right">This resource is available under the Open Government Licence (OGL)</a>
      <#else>
  	   <p>
  	     <a href="${downloadOrder.licenseUrl}">Licence terms and conditions apply</a>
  	   </p>
      </#if>
    <#else>
      <p>Licence not found</p>
    </#if>
    </div>
</div>
</#if>
