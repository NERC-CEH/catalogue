<div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

<#if title?has_content>
  <h1 id="document-title">
  
  <#if (resourceType.value)?has_content>
    <small id="resource-type">${codes.lookup('metadata.scopeCode', resourceType.value)!''}</small><br>
  </#if>
  ${title?html}
  <#if (metadata.state == 'draft' || metadata.state == 'pending') >
    <small> - ${codes.lookup('publication.state', metadata.state)!''}</small>
  </#if>
  </h1>
</#if>