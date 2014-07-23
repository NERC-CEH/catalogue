<#if boundingBoxes?has_content && boundingBoxes??>
  <h3>Extent</h3>
    <#list boundingBoxes as extent>
      <img src="${extent.googleStaticMapUrl}" alt="Extent">
    </#list>
</#if>