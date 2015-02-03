<#if parentIdentifier?has_content || (children?size gt 0 && children??) || (documentLinks?size gt 0 && documentLinks??)>
  <div class="panel panel-default" id="document-children">
    <div class="panel-body">
      <#if parentIdentifier?has_content>
        <h4>This ${resourceType?html} is part of the series:</h4>
        <p><a href="${parent.href?html}">${parent.title?html}</a></p>
      </#if>
      <#if children?size gt 0 && children??>
        <h4>This data series comprises the following datasets:</h4>
        <#list children as child>
          <p><a href="${child.href?html}">${child.title?html}</a></p>
        </#list>
      </#if>
      <#include "_related.html.tpl">
    </div>
  </div>
</#if>