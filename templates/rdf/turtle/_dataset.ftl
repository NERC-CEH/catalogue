a dcat:Dataset, dcmitype:Dataset ;
dct:type dcmitype:Dataset ;

<#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
  dct:available "${datasetReferenceDate.publicationDate}"^^xsd:date ;
</#if>

 dcat:landingPage <${uri}><#if datacitable?string=="true" && citation?has_content>, <${citation.url?trim}></#if> ;

 <#include "_rights.ftl"> <#--rights at DATASET level-->

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
    <#include "_rights.ftl"> <#--rights at DISTRIBUTION level-->
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
