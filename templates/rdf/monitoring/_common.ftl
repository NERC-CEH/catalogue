<#ftl output_format="plainText">

 <#if resourceIdentifiers?? && resourceIdentifiers?has_content && resourceIdentifiers?filter(ri -> ri.codeSpace?has_content)?has_content>
  <#assign localIDs = resourceIdentifiers?filter(ri -> ri.codeSpace?has_content)?filter(ri -> !ri.codeSpace?starts_with("doi")) >
 </#if>

<#macro displayLiteral string>
  <#--Ensure literals do not contain " characters-->
  <#t>"${string?trim?replace("\"","'")?replace("\n"," ")}"
</#macro>

<#macro common rdftype="" other="" prefixed=true>
  <#if prefixed>
  PREFIX : <${uri?replace(id,"")}>
  PREFIX dcterms: <http://purl.org/dc/terms/>
  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
  PREFIX doo: <https://digital.ceh.ac.uk/ontology/doo/>
  PREFIX sosa: <http://www.w3.org/ns/sosa/>
  PREFIX geo: <http://www.opengis.net/ont/geosparql#>
  PREFIX adms: <http://www.w3.org/ns/adms#>
  </#if>

  :${id}
    a ${rdftype} ;
    dcterms:title "${title}" ;

    <#if localIDs?has_content>
        adms:identifier <@idList localIDs/> ;
    </#if>

    <#if description?has_content>
      dcterms:description <@displayLiteral description /> ;
    </#if>

    <#if boundingBox?has_content>
      geo:hasBoundingBox :bbox ;
    </#if>

    <#nested>

    dcterms:language <http://id.loc.gov/vocabulary/iso639-1/en>;
    <#-- other triples not about <id>, e.g. authors, organisations -->
    ${other}

    <#-- Relationships between records -->
    <@jenaLinks "http://purl.org/dc/terms/related" />
    <@jenaLinks "http://www.w3.org/2004/02/skos/core#narrower" />
    <@jenaLinks "http://purl.org/dc/terms/replaces" />
    <@jenaLinks "http://purl.org/dc/terms/isPartOf" />
    <@jenaLinks "https://digital.ceh.ac.uk/ontology/doo/uses" />
    <@jenaLinks "https://digital.ceh.ac.uk/ontology/doo/utilises" />
    <@jenaLinks "https://digital.ceh.ac.uk/ontology/doo/triggers" />
    .

    <#if localIDs?has_content>
      <@idNodes localIDs/>
    </#if>

    <#if boundingBox?has_content>
    <#-- Bounding box node -->
      :bbox
          a geo:Geometry ;
          geo:asEnvelope "ENVELOPE(${boundingBox.westBoundLongitude}, ${boundingBox.eastBoundLongitude}, ${boundingBox.southBoundLatitude}, ${boundingBox.northBoundLatitude})"^^geo:envelopeLiteral .
    </#if>

</#macro>

<#macro jenaLinks predicate="http://purl.org/dc/terms/related">
  <#local links=jena.relationships(uri, predicate) />
  <#if links?has_content>
    ${predicate} <#t><#list links as link>
      <${link.href}><#sep>, <#sep><#t>
    </#list>;
  </#if>
</#macro>

<#macro idList ids>
  <#list ids as id>
    <#if id.codeSpace?has_content && !id.codeSpace?starts_with("doi")>
    :id${id?index}<#sep>, <#sep><#t>
    </#if>
  </#list>
</#macro>

<#macro idNodes ids>
  <#list ids as id>
  <#if id.codeSpace?has_content && !id.codeSpace?starts_with("doi")>
  :id${id?index} a adms:Identifier ;
      skos:notation "${id.code}" ;
      adms:schemaAgency <https://ror.org/00pggkr55> ;
      dcterms:conformsTo "UKCEH" ;
      .
    </#if>
  </#list>
</#macro>
