<#import "../functions.ftlh" as func>
<#compress>
<#setting date_format = 'yyyy-MM-dd'>
<#if useConstraints?has_content>
  <#assign licences = func.filter(useConstraints, "code", "license")>
</#if>
<#if responsibleParties?has_content>
  <#assign
    authors = func.filter(responsibleParties, "role", "author")
    pointsOfContact = func.filter(responsibleParties, "role", "pointOfContact")
    publishers  = func.filter(responsibleParties, "role", "publisher")
    >
</#if>

@prefix dcat: <http://www.w3.org/ns/dcat#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix dcmitype: <http://purl.org/dc/dcmitype/> .
@prefix geo: <http://www.opengis.net/ont/geosparql#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

<${uri}>
  dct:title "${title}" ;
  <#if description?has_content>
  dct:description "${description?replace("\n", " ")}" ;
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

  <#--Points of contact-->
  <#if pointsOfContact?has_content>
    dcat:contactPoint
    <#list pointsOfContact as poc>
      [
      <#if poc.individualName?has_content>
        a vcard:Individual ;
        vcard:n "${poc.individualName}" ;
        <#if poc.nameIdentifier?has_content && poc.nameIdentifier?starts_with("http")> vcard:hasUID <${poc.nameIdentifier}> ;</#if>
        <#if poc.organisationName?has_content>vcard:organization-name "${poc.organisationName}" ;</#if>
      <#else>
        a vcard:Organization ;
        vcard:fn "${poc.organisationName}" ;
        <#if poc.organisationIdentifier?has_content && poc.organisationIdentifier?starts_with("http")> vcard:hasUID <${poc.organisationIdentifier}"> ;</#if>
      </#if>
      <#if poc.email?has_content>vcard:hasEmail "${poc.email}" ;</#if>
      ]<#sep>,
    </#list>
   ;
  </#if>

  <#--Publisher-->
  <#if publishers?has_content>
  dct:publisher
  <#list publishers as publisher>
    [
    a vcard:Organization ;
    vcard:fn "${publisher.organisationName}" ;
    <#if publisher.email?has_content>vcard:hasEmail "${publisher.email}" ;</#if>
    <#if publisher.organisationIdentifier?has_content && publisher.organisationIdentifier?starts_with("http")> vcard:hasUID <${publisher.organisationIdentifier}> ;
    </#if>
    ]<#sep>,
  </#list>
   ;
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

  <#-- Keywords -->
  <#assign allKeywords = []>
  <#if descriptiveKeywords?has_content>
    <#list descriptiveKeywords as descriptiveKeyword>
        <#assign allKeywords = allKeywords + descriptiveKeyword.keywords >
    </#list>
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


  <#if type=='dataset' || type=='nonGeographicDataset'>
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
</#compress>
