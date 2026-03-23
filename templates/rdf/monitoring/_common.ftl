<#ftl output_format="plainText">


<#macro displayLiteral string>
  <#--Ensure literals do not contain " characters-->
  <#t>"${string?trim?replace("\"","'")?replace("\n"," ")}"
</#macro>

<#macro common rdftype="" other="" prefixed=true>
  <#if prefixed>
  PREFIX : <${uri?replace(id,"")}>
  PREFIX dcterms: <http://purl.org/dc/terms/>
  PREFIX skos: <http://www.w3.org/2008/05/skos#>
  PREFIX ef: <http://onto.ceh.ac.uk/EF#>
  PREFIX sosa: <http://www.w3.org/ns/sosa/>
  PREFIX geo: <http://www.opengis.net/ont/geosparql#>
  </#if>

  :${id}
    a ${rdftype} ;
    dcterms:title "${title}" ;
    <#if description?has_content>
      dcterms:description <@displayLiteral description /> ;
    </#if>
    <#if boundingBox?has_content>
      ef:boundingBox "POLYGON${boundingBox.coordinates?replace('[[[','((')?replace(']]]','))')?replace('[^]], ',' ','r')?replace(']', '')?replace('[', '')}"^^geo:wktLiteral ;
    </#if>

    <#nested>

    dcterms:language <http://id.loc.gov/vocabulary/iso639-1/en>;
    <#-- other triples not about <id>, e.g. authors, organisations -->
    ${other}

    <#-- Relationships between records -->
    <@c.jenaLinks "http://purl.org/dc/terms/related" />
    <@c.jenaLinks "http://www.w3.org/2004/02/skos/core#narrower" />
    <@c.jenaLinks "http://purl.org/dc/terms/replaces" />
    <@c.jenaLinks "http://purl.org/dc/terms/isPartOf" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/uses" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/utilises" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/triggers" />
    .
</#macro>

<#macro jenaLinks predicate="http://purl.org/dc/terms/related">
  <#local links=jena.relationships(uri, predicate) />
  <#if links?has_content>
    ${predicate} <#t><#list links as link>
      <${link.href}><#sep>, <#sep><#t>
    </#list>;
  </#if>
</#macro>
