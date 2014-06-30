<#if downloadOrder?has_content>
<div class="panel panel-default">
  <div class="panel-heading"><p class="panel-title">Get the data</p></div>
    <div class="panel-body">
      <p>
        Preview
      </p>
      <p>
        <a href="${downloadOrder.orderUrl}">Order/download</a>
      </p>
      <p>
        <a href="${downloadOrder.supportingDocumentsUrl}">Supporting documentation</a>
      </p>
    </div>
    <div class="panel-footer column-sm">
    <#if downloadOrder.isOgl()>
      <a href="${downloadOrder.licenseUrl}"><img src="/static/img/ogl.png" alt="OGL" class="pull-right">This resource is available under the Open Government Licence (OGL)</a>
      <#else>
	  <p>
	  <a href="${downloadOrder.licenseUrl}">Licence terms and conditions apply</a>
	  </p>
    </#if>
    </div>
</div>
</#if>