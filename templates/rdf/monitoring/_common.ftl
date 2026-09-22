<#ftl output_format="plainText">
<#import "../_turtle.ftl" as ttl>

 <#if resourceIdentifiers?? && resourceIdentifiers?has_content && resourceIdentifiers?filter(ri -> ri.codeSpace?has_content)?has_content>
  <#assign localIDs = resourceIdentifiers?filter(ri -> ri.codeSpace?has_content)?filter(ri -> !ri.codeSpace?starts_with("doi")) >
 </#if>

<#macro displayLiteral string>
  <#t>"${ttl.escape(string?trim)}"
</#macro>

<#function displayNamespace text>
  <#local myReplacements = [
    { "full": "http://www.w3.org/2004/02/skos/core#", "short": "skos:" },
    { "full": "https://digital.ceh.ac.uk/ontology/doo/", "short": "doo:" },
    { "full": "http://purl.org/dc/terms/", "short": "dcterms:" }
  ]>
  <#local result = text>
  <#list myReplacements as r>
    <#local result = result?replace(r.full, r.short)>
  </#list>
  <#return result>
</#function>


<#macro common rdftype="" other="" prefixed=true>
  <#if prefixed>
  PREFIX : <${uri?replace(id,"")}>
  PREFIX dcterms: <http://purl.org/dc/terms/>
  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
  PREFIX ef: <http://onto.ceh.ac.uk/EF#>
  PREFIX doo: <https://digital.ceh.ac.uk/ontology/doo/>
  PREFIX sosa: <http://www.w3.org/ns/sosa/>
  PREFIX geo: <http://www.opengis.net/ont/geosparql#>
  PREFIX adms: <http://www.w3.org/ns/adms#>
  </#if>

  :${id}
    a ${rdftype} ;
    dcterms:title "${ttl.escape(title)}" ;

    <#if localIDs?has_content>
        adms:identifier <@idList localIDs/> ;
    </#if>

    <#if description?has_content>
      dcterms:description <@displayLiteral description /> ;
    </#if>

    <#if boundingBox?has_content>
      geo:hasBoundingBox :${id}_bbox ;
    </#if>

    <#nested>

    dcterms:language <http://id.loc.gov/vocabulary/iso639-1/en>;
    <#-- other triples not about <id>, e.g. authors, organisations -->
    ${other}

    <#-- Relationships between records -->
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme" />
    <@c.jenaLinks "http://purl.org/dc/terms/relation" />
    <@c.jenaLinks "http://purl.org/dc/terms/replaces" />
    <@c.jenaLinks "http://purl.org/dc/terms/isPartOf" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/uses" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/utilises" />
    <@c.jenaLinks "https://digital.ceh.ac.uk/ontology/doo/triggers" />
    .

    <#if localIDs?has_content>
      <@idNodes localIDs/>
    </#if>

    <#if boundingBox?has_content>
    <#-- Bounding box node -->
      :${id}_bbox
          a geo:Geometry ;
          geo:asEnvelope "ENVELOPE(${boundingBox.westBoundLongitude}, ${boundingBox.eastBoundLongitude}, ${boundingBox.southBoundLatitude}, ${boundingBox.northBoundLatitude})"^^geo:envelopeLiteral .
    </#if>
</#macro>

<#macro jenaLinks predicate="http://purl.org/dc/terms/relation">
  <#local links=jena.relationships(uri, predicate) />
  <#if links?has_content>
    ${displayNamespace(predicate)} <#t>
    <#list links as link>
        <${link.href}><#sep>, <#sep><#t>
    </#list>;
  </#if>
</#macro>

<#macro idList ids>
  <#assign docId = id>
  <#list ids as id>
    <#if id.codeSpace?has_content && !id.codeSpace?starts_with("doi")>
    :${docId}_id${id?index}<#sep>, <#sep><#t>
    </#if>
  </#list>
</#macro>

<#macro idNodes ids>
  <#assign docId = id>
  <#list ids as id>
  <#if id.codeSpace?has_content && !id.codeSpace?starts_with("doi")>
  :${docId}_id${id?index} a adms:Identifier ;
      skos:notation "${ttl.escape(id.code)}" ;
      adms:schemaAgency <https://ror.org/00pggkr55> ;
      dcterms:conformsTo "UKCEH" ;
      .
    </#if>
  </#list>
</#macro>
