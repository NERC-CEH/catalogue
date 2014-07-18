<#if boundingBoxes?has_content && boundingBoxes??>
  <h3>Extent</h3>
  <p>
  <#list boundingBoxes as extent>
    <img property="dc:spatial" src="${extent.googleStaticMapUrl}" alt="Extent">
  </#list>
  </p>
</#if>