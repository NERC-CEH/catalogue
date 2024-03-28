<#ftl output_format="plainText">
<#macro common rdftype="" other="">
PREFIX : <${uri?replace(id,"")}>
PREFIX dct: <http://purl.org/dc/terms/>
PREFIX skos: <http://www.w3.org/2008/05/skos#>
PREFIX ef: <http://www.w3.org/2015/03/inspire/ef#>
PREFIX sosa: <http://www.w3.org/ns/sosa/>
PREFIX geo: <http://www.opengis.net/ont/geosparql#>

:${id}
  a ${rdftype} ;
  dct:title "${title}" ;
  <#if description?has_content>
    dct:description '''${description}''' ; <#-- triple single quote handles multi-line strings -->
  </#if>
  <#if boundingBox?has_content>
    ef:boundingBox "POLYGON${boundingBox.coordinates?replace('[[[','((')?replace(']]]','))')?replace('[^]], ',' ','r')?replace(']', '')?replace('[', '')}"^^geo:wktLiteral ;
  </#if>

  <#nested>

  dct:language "eng" .
</#macro>

