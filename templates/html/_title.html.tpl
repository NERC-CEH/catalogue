<div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

<#if title?has_content>
  <h1 id="document-title" property="dc:title">
  
  <#if (resourceType.value)?has_content>
  <small class="resource-type ${resourceType}" property="dc:type">${resourceType.value}</small>
  </#if>
  
  ${title}
  
  </h1>
</#if>