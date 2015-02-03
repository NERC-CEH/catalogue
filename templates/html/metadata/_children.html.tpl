<#if doc.parentIdentifier?has_content || (doc.children?size gt 0 && doc.children??) || (doc.documentLinks?size gt 0 && doc.documentLinks??)>
  <div class="panel panel-default" id="document-children">
    <div class="panel-body">
      <#if doc.parentIdentifier?has_content>
        <h4>This ${doc.resourceType?html} is part of the series:</h4>
        <p><a href="${doc.parent.href?html}">${doc.parent.title?html}</a></p>
      </#if>
      <#if doc.children?size gt 0 && doc.children??>
        <h4>This data series comprises the following datasets:</h4>
        <#list doc.children as child>
          <p><a href="${child.href?html}">${child.title?html}</a></p>
        </#list>
      </#if>
      <#include "_related.html.tpl">
    </div>
  </div>
</#if>