<#import "../functions.ftlh" as func>

<#macro contactList contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>
      <#assign contactIdentifier="_:" + prefix + contact?counter>
      
      <#if contact.individualName?has_content>
        <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
          <#assign contactIdentifier= "\l" + contact.nameIdentifier + "\g">
        </#if>
      <#else>
        <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^http(|s)://ror.org/[0-9a-z]+$")>
          <#assign contactIdentifier="\l" + contact.organisationIdentifier+ "\g">
        </#if>
      </#if>
      
      ${contactIdentifier?no_esc}<#sep>,</#sep>
    </#list>
  </#if>
</#macro>

<#macro contactDetail contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>
      <#assign contactIdentifier="_:" + prefix + contact?counter>

      <#if contact.individualName?has_content>
        <#assign contactType="vcard:Individual">
        <#assign contactName=contact.individualName>
        <#if contact.organisationName?has_content>
          <#assign orgName=contact.organisationName>
        </#if>
        <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
          <#assign contactIdentifier="\l" + contact.nameIdentifier + "\g">
        </#if>
      <#elseif contact.organisationName?has_content >
        <#assign contactType="vcard:Organization">
        <#assign contactName=contact.organisationName>
        <#assign orgName="">
        <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^http(|s)://ror.org/[0-9a-z]+$")>
          <#assign contactIdentifier="\l" + contact.organisationIdentifier+ "\g">
        </#if>
      </#if>
    
      ${contactIdentifier?no_esc} a ${contactType} ;
	    vcard:fn "${contactName}" ;
      <#if orgName?has_content>vcard:organization-name "${orgName}" ;</#if>
      <#if contact.email?has_content>vcard:hasEmail "${contact.email}" ;</#if>
      .
    </#list>
  </#if>
</#macro>


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
<@contactDetail authors "a" />

</#compress>
