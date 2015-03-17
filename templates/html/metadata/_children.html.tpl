<#if parentIdentifier?has_content || (children?size gt 0 && children??) || (documentLinks?size gt 0 && documentLinks??)>
  <div class="panel panel-default" id="document-children">
    <div class="panel-heading">
      <#if parentIdentifier?has_content>
        This ${resourceType?html} is part of the series
      </#if>
      <#if children?size gt 0 && children??>
        This data series comprises the following datasets
      </#if>
	</div>
    <div class="panel-body">
      <#if parentIdentifier?has_content>
        <p><a href="${parent.href?html}">${parent.title?html}</a></p>
      </#if>
      <#if children?size gt 0 && children??>
        <#list children as child>
          <p><a href="${child.href?html}">${child.title?html}</a></p>
        </#list>
      </#if>
    </div>
  </div>
</#if>


