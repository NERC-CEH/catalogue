<#import "../functions.tpl" as func>
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
  dct:description "${description}" ;
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
        <#if poc.organisationName?has_content>vcard:organization-name "${poc.organisationName}" ;</#if>
      <#else>
        a vcard:Organization ;
        vcard:fn "${poc.organisationName}" ;
      </#if>
      <#if poc.email?has_content>vcard:hasEmail "${poc.email}" ;</#if>
      <#if poc.nameIdentifier?has_content && poc.nameIdentifier?starts_with("http")>
        vcard:hasUID <${poc.nameIdentifier}"> ;
      </#if>
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
    <#if publisher.nameIdentifier?has_content && publisher.nameIdentifier?starts_with("http")>
      vcard:hasUID <${publisher.nameIdentifier}> ;
    </#if>
    ]<#sep>,
  </#list>
   ;
  </#if>
  
  dct:language "eng" ;
  <#if parentIdentifier?has_content>
   dct:isPartOf <https://catalogue.ceh.ac.uk/id/${parentIdentifier}> ;
  </#if>

  <#-- Subjects -->
  <#if descriptiveKeywords?has_content>
  dct:subject 
    <#list descriptiveKeywords as descriptiveKeyword>
    <#list descriptiveKeyword.keywords as keyword>
      <#if keyword.uri?has_content>
        <#if keyword.value?has_content>
          <${keyword.uri}>, "${keyword.value}"<#sep>,
        <#else>
          "${keyword.uri}"<#sep>,
        </#if>
      <#else>
        "${keyword.value}"<#sep>,
      </#if>
    </#list><#sep>,
    </#list>
  ;
  </#if>

  <#if type=='dataset' || type=='nonGeographicDataset'>
    <#include "turtle/_dataset.tpl">
  <#elseif type=='aggregate'|| type=='collection'|| type=='series'>
    <#include "turtle/_aggregation.tpl">
  <#elseif type=='service'>
    <#include "turtle/_service.tpl">
  <#elseif type=='application'>
    <#include "turtle/_application.tpl">
  <#else>
  </#if>
.
</#compress>