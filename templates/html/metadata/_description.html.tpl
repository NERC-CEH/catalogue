<#if description?has_content>
  <p id="document-description" property="dc:abstract">${description?html?replace("\n", "<br>")}</p>
</#if>
