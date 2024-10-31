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
  PREFIX ef: <http://onto.ceh.ac.uk/EF#>
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

    dct:language "eng";
    <#-- other triples not about <id>, e.g. authors, organisations -->
    ${other}

    <#-- Relationships between records -->
    <@c.jenaLinks "associatedWith" />
    <@c.jenaLinks "narrower" />
    <@c.jenaLinks "supersedes" />
    <@c.jenaLinks "belongsTo" />
    <@c.jenaLinks "uses" />
    <@c.jenaLinks "utilises" />
    <@c.jenaLinks "hasChild" />
    <@c.jenaLinks "triggers" />
    .
</#macro>

<#macro jenaLinks predicate="associatedWith">
  <#local predicateURI="http://onto.ceh.ac.uk/EF#" + predicate>
  <#local links=jena.relationships(uri, predicateURI) />
  <#if links?has_content>
    ef:${predicate} <#t><#list links as link>
      <${link.href}><#sep>, <#sep><#t>
    </#list>;
  </#if>
</#macro>
