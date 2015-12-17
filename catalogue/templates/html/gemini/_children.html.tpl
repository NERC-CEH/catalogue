<#if children?size gt 0 && children??>
  <div class="panel panel-default" id="document-children">
    <div class="panel-heading">
      <p class="panel-title">This data series comprises the following datasets</p>
    </div>
    <div class="panel-body">
      <#list children as child>
        <p><a href="${child.href?html}">${child.title?html}</a></p>
      </#list>
    </div>
  </div>
</#if>
<#if parent?has_content>
  <div class="panel panel-default" id="document-children">
    <div class="panel-heading">
      <p class="panel-title">This ${resourceType.value?html} is part of the series</p>
    </div>
    <div class="panel-body">
      <p><a href="${parent.href?html}">${parent.title?html}</a></p>
    </div>
  </div>
</#if>