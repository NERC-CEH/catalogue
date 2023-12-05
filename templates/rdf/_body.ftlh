:${id}
  dct:title "${title}" ;
  <#if description?has_content>
  dct:description "${description?replace("\n", " ")}" ;
  </#if>

  <#if lineage?has_content>
  dct:provenance [ a dct:ProvenanceStatement ;
      rdfs:label "${lineage?replace("\n", " ")}"  ] ;
  </#if>

  <#if boundingBoxes?has_content && boundingBoxes?has_content>
    <#list boundingBoxes as extent>
       dct:spatial "${extent.wkt}"^^geo:wktLiteral ;
    </#list>
  </#if>
  <#if temporalExtents?has_content>
    <#list temporalExtents as extent>
       dct:temporal "${(extent.begin?date)!''}/${(extent.end?date)!''}"^^dct:PeriodOfTime ;
    </#list>
  </#if>

  <#--Points of contact2-->
  <#if pointsOfContact?has_content>
    dcat:contactPoint <@contactList pointsOfContact "c" />  ;
  </#if>

  <#--Publisher-->
  <#if publishers?has_content>
    dct:publisher  <@contactList publishers "pub" /> ;
  </#if>

  dct:language "eng" ;
  <#assign rel_memberOf = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")>
  <#if rel_memberOf?has_content && rel_memberOf?size gt 0>
  dct:isPartOf
    <#list rel_memberOf as item>
      <${item.href}><#sep>,
    </#list>
  ;
  </#if>

  <#if allKeywords?has_content>
  dct:subject
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

  <#if elterProject?has_content>
   prov:wasGeneratedBy <@projectList /> ;
  </#if>

  <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
    <#include "turtle/_dataset.ftlh">
  <#elseif type=='aggregate'|| type=='collection'|| type=='series'>
    <#include "turtle/_aggregation.ftlh">
  <#elseif type=='service'>
    <#include "turtle/_service.ftlh">
  <#elseif type=='application'>
    <#include "turtle/_application.ftlh">
  <#else>
  </#if>
.

<#if pointsOfContact?has_content>
  <@contactDetail pointsOfContact "c" />
</#if>

<#if publishers?has_content>
  <@contactDetail publishers "pub" />
</#if>

<#if authors?has_content>
  <@contactDetail authors "a" />
</#if>

<#if allKeywords?has_content>
  <#list allKeywords as keyword>
    <#if keyword.uri?has_content && keyword.value?has_content>
        <${keyword.uri}> rdfs:label "${keyword.value}" .
      </#if>
  </#list>
</#if>

<@projectDetail />