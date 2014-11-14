<div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

<#if title?has_content>
  <h1 id="document-title" property="dc:title" content="${title?html}">
  
  <#if (resourceType)?has_content>
    <small id="resource-type" property="dc:type">${resourceType?html}</small>
  </#if><br>
  
  ${title?html}
  
  </h1>
</#if>
