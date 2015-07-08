<#if description?has_content>
  <p id="document-description">${description?html?replace("\n", "<br>")}</p>
</#if>
