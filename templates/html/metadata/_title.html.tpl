<div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

<#if title?has_content>
  <h1 id="document-title" property="dc:title" content="${title?html}">
  
  <#if (resourceType)?has_content>
    <small id="resource-type" property="dc:type">${codes.lookup('metadata.scopeCode', resourceType)!''}</small>
  </#if><br>
  ${title?html}
  <#if (metadata.state == 'draft' || metadata.state == 'pending') >
    <small> - ${codes.lookup('publication.state', metadata.state)!''}</small>
  </#if>
  </h1>
</#if>
