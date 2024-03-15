<#ftl output_format="plainText">
<#macro common other="">
BASE <${uri?replace(id,"")}>
PREFIX dct: <http://purl.org/dc/terms/>

<${id}>
  dct:title "${title}" ;
  <#if description?has_content>
    dct:description "${description?replace("\n", " ")}" ;
  </#if>
  <#nested> <#-- includes more specific statements about <id> here -->
  dct:language "eng" . <#-- closes statements about <id> -->
<#-- other triples not about <id>, e.g. authors, organisations -->
${other} <#-- includes other triples here -->
</#macro>
