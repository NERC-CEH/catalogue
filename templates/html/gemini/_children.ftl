<#if children??>
  <#list children?sort_by("title")>
    <div class="panel panel-default" id="document-children">
      <div class="panel-heading">
        <p class="panel-title">This ${recordType} includes the following resources</p>
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
  <div class="panel panel-default" id="document-parent">
    <div class="panel-heading">
      <p class="panel-title">This ${recordType} is part of the following</p>
    </div>
    <div class="panel-body">
      <p><a href="${parent.href?html}">${parent.title?html}</a></p>
    </div>
  </div>
</#if>