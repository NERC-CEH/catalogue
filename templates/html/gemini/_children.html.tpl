<#if children??>
  <#list children>
    <div class="panel panel-default">
      <div class="panel-heading">
        <p class="panel-title">This data ${resourceType.value?html} comprises the following resources</p>
      </div>
      <div class="panel-body">
        <#items as child>
          <p><a href="${child.href?html}">${child.title?html}</a></p>
        </#items>
      </div>
    </div>
  </#list>
</#if>
<#if parent?has_content>
  <div class="panel panel-default">
    <div class="panel-heading">
      <p class="panel-title">This ${resourceType.value?html} is part of</p>
    </div>
    <div class="panel-body">
      <p><a href="${parent.href?html}">${parent.title?html}</a></p>
    </div>
  </div>
</#if>