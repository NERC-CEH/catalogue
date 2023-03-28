a dcat:Dataset, dcmitype:Dataset ;

<#if responsibleParties?has_content>
  <#assign
    authors = func.filter(responsibleParties, "role", "author")
    pointsOfContact = func.filter(responsibleParties, "role", "pointOfContact")
    publishers  = func.filter(responsibleParties, "role", "publisher")
    >
</#if>
<#if onlineResources?has_content>
  <#assign downloads = func.filter(onlineResources, "function", "download") + func.filter(onlineResources, "function", "order")>
</#if>
<#if descriptiveKeywords?has_content>
  <#assign
  themelist = func.filter(descriptiveKeywords, "type", "Catalogue topic")
  >
</#if>

<#if resourceIdentifiers??>
  dct:identifier
    <#list resourceIdentifiers as id>
    <#if id.coupledResource?starts_with("http")>
      <${id.coupledResource}>
    <#else>
      "${id.coupledResource}"
    </#if><#sep>,
    </#list>
    ;
</#if>

dcat:CatalogRecord <${uri}> ;
dct:type dcmitype:Dataset ;

<#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
  dct:available "${datasetReferenceDate.publicationDate}" ;
</#if>

 dcat:landingPage <${uri}><#if datacitable?string=="true" && citation?has_content>, <${citation.url}></#if>;

<#if datacitable?string=='true' && citation?has_content>
    dct:bibliographicCitation "${citation.authors?join(' ,')} (${citation.year?string("0")}). ${citation.title}. ${citation.publisher}. ${citation.url}" ;
</#if>

<#-- Themes-->
<#if themelist?has_content>
  dcat:theme
  <#list themelist as themes>
    <#list themes.keywords as theme>
      <#if theme.uri?has_content>
        <${theme.uri}><#sep>,
      </#if>
    </#list><#sep>,
  </#list>
  ;
</#if>

<#-- Keywords -->
<#if allKeywords?has_content>
cat:keyword
  <#list allKeywords as keyword>
    <#if keyword.uri?has_content>
      <#if keyword.value?has_content>
        <${keyword.uri}>, "${keyword.value}"<#t>
      <#else>
        "${keyword.uri}"<#t>
      </#if>
    <#else>
      "${keyword.value}"<#t>
    </#if><#sep>,</#sep><#t>
  </#list>
;
</#if>

<#--Distribution-->
<#if downloads?has_content>
dcat:Distribution [
    dcat:accessURL
    <#list downloads as download>
      <${download.url}> <#sep>,
    </#list>
    ;
    <#include "_rights.ftlh">
    <#if distributionFormats?has_content>
    dct:format
      <#list distributionFormats as format>
      [
      a dct:IMT ;
      rdf:value "${format.name}" ; rdfs:label "${format.name}"
      ] <#sep>,
      </#list>
      ;
    </#if>
];
</#if>

<#--Authors-->
<#if authors?has_content>
dcat:contributor
<#include "_authors.ftlh">
;
</#if>

<#assign rel_memberOf = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")>
<#if rel_memberOf?has_content && rel_memberOf?size gt 0>
dct:isPartOf
  <#list rel_memberOf as item>
    <${item.href}><#sep>,
  </#list>
;
</#if>
