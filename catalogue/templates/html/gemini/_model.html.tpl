<#if modelLinks??>
  <#list modelLinks>
  <div class="panel panel-default" id="document-children">
    <div class="panel-heading">
      <p class="panel-title">Models</p>
    </div>
    <div class="panel-body">
    <#items as model>
      <p><a href="${model.href?html}">${model.title?html}</a></p>
    </#items>
    </div>
  </div>
  </#list>
</#if>
<#if modelApplicationLinks??>
  <#list modelApplicationLinks>
  <div class="panel panel-default" id="document-children">
    <div class="panel-heading">
      <p class="panel-title">Model Applications</p>
    </div>
    <div class="panel-body">
    <#items as modelApplication>
      <p><a href="${modelApplication.href?html}">${modelApplication.title?html}</a></p>
    </#items>
    </div>
  </div>
  </#list>
</#if>