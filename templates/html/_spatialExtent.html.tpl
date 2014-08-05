<#if boundingBoxes?has_content && boundingBoxes??>
  <#list boundingBoxes as extent>
    <img property="dc:spatial" src="${extent.googleStaticMapUrl}" alt="Spatial Extent">
  </#list>
</#if>