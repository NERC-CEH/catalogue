<div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

<#if title?has_content>
  <h1 id="document-title" property="dc:title" content="${title}">
  
  <#if (resourceType)?has_content>
    <small id="resource-type" property="dc:type">${resourceType}</small>
  </#if><br>
  
  ${title}
  
  </h1>
</#if>
