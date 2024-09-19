<#ftl output_format="plainText">


<#macro displayLiteral string>
  <#--Ensure literals do not contain " characters-->
  <#t>${string?replace("\"","'")?replace("\n"," ")}
</#macro>

<#macro common rdftype="" other="" prefixed=true>
  <#if prefixed>
  PREFIX : <${uri?replace(id,"")}>
  PREFIX dct: <http://purl.org/dc/terms/>
  PREFIX skos: <http://www.w3.org/2008/05/skos#>
  PREFIX ef: <http://www.w3.org/2015/03/inspire/ef#>
  PREFIX sosa: <http://www.w3.org/ns/sosa/>
  PREFIX geo: <http://www.opengis.net/ont/geosparql#>
  </#if>

  :${id}
    a ${rdftype} ;
    dct:title "${title}" ;
    <#if description?has_content>
      dct:description "<@displayLiteral description />" ;
    </#if>
    <#if boundingBox?has_content>
      ef:boundingBox "POLYGON${boundingBox.coordinates?replace('[[[','((')?replace(']]]','))')?replace('[^]], ',' ','r')?replace(']', '')?replace('[', '')}"^^geo:wktLiteral ;
    </#if>

    <#nested>

    dct:language "eng" .
    <#-- other triples not about <id>, e.g. authors, organisations -->
    ${other} <#-- includes other triples here -->
</#macro>
