<div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

<#if doc.title?has_content>
  <h1 id="document-title" property="dc:title" content="${doc.title?html}">
  
  <#if (doc.resourceType)?has_content>
    <small id="resource-type" property="dc:type">
      ${codes.lookup('metadata.scopeCode', doc.resourceType)!''}
    </small>
  </#if><br>
  
  ${doc.title?html}
  </h1>
</#if>
