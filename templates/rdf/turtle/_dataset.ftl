a dcat:Dataset, dcmitype:Dataset ;

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

dct:type dcmitype:Dataset ;

<#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
  dct:available "${datasetReferenceDate.publicationDate}" ;
</#if>

 dcat:landingPage <${uri}><#if datacitable?string=="true" && citation?has_content>, <${citation.url?trim}></#if>;

<#if datacitable?string=='true' && citation?has_content>
    dct:bibliographicCitation "${citation.authors?join(' ,')} (${citation.year?string("0")}). ${citation.title}. ${citation.publisher}. ${citation.url?trim}" ;
</#if>

<#if keywordsTheme?has_content>
  dct:theme <@keywordList keywordsTheme/> ;
</#if>

<#--Distribution-->
<#list downloads>
dcat:distribution [
    dcat:accessURL
    <#items as download>
      <${download.url?trim}> <#sep>,
    </#items>
    ;
    <#include "_rights.ftl">
    <#list distributionFormats>
    dct:format
      <#items as format>
      [
      a dct:IMT ;
      rdf:value "${format.name}" ; rdfs:label "${format.name}"
      ] <#sep>,
      </#items>
      ;
    </#list>
];
</#list>

<#--Authors-->
<#if authors?has_content>
  dct:contributor <@contactList authors "a" /> ;
</#if>
