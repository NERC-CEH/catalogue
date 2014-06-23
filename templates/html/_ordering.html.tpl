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
    <div class="panel-footer">
    	<#if isOgl == True>
    	<p>
          <a href="${downloadOrder.licenseUrl}">This resource is available under the Open Government Licence (OGL)<img src="/static/img/ogl.png" alt="OGL"></a>
        </p>
        <#else>
        <p>
          This resource is licensed
        </p>
        </#if>
    </div>
</div>
</#if>