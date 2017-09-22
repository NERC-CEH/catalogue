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
  themelist = func.filter(descriptiveKeywords, "type", "INSPIRE Theme") +  func.filter(descriptiveKeywords, "type", "CEH Topic")
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
dct:type <http://purl.org/dc/dcmitype/Dataset> ;

<#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
  dct:available "${datasetReferenceDate.publicationDate}" ;
</#if>
 
 dcat:landingPage <${uri}> <#if datacitable?string=="true" && citation?has_content>,${citation.url}</#if>;

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
<#if descriptiveKeywords?has_content>
  dcat:keyword 
  <#list descriptiveKeywords as descriptiveKeyword>
  <#list descriptiveKeyword.keywords as keyword>
    <#if keyword.uri?has_content>
      <${keyword.uri}><#sep>,
    <#else>
      "${keyword.value}"<#sep>,
    </#if>
  </#list><#sep>,
  </#list> ;
</#if>

<#--Distribution-->
<#if downloads?has_content>
dcat:Distribution [
    dcat:accessURL
    <#list downloads as download>
      <${download.url}> <#sep>,
    </#list>
    ;
    <#include "_rights.tpl">
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
<#include "_authors.tpl">
;
</#if>

<#--Associated resources-->
<#if associatedResources?has_content>
<#assign parents = func.filter(associatedResources, "associationType", "series") + func.filter(associatedResources, "associationType", "aggregate") + func.filter(associatedResources, "associationType", "collection")>
  <#if parents?has_content>
  dct:isPartOf
    <#list parents as parent>
    <${parent.href}><#sep>,
    </#list>
  ;
  </#if>
</#if>