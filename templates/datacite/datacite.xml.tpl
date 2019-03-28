<#import "../underscore.tpl" as _>
<#import "../functions.tpl" as func>
<#assign authors = _.filter(doc.responsibleParties, _.isAuthor) >
<#compress>
<#escape x as x?xml>
<?xml version="1.0" encoding="UTF-8"?>
<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://datacite.org/schema/kernel-4" xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd">
  <identifier identifierType="DOI">${doi}</identifier>
  
  <#include "_creators.xml.tpl">

  <titles>
    <title>${doc.title}</title>
  </titles>
  <publisher>NERC Environmental Information Data Centre</publisher>
  <publicationYear><#if doc.datasetReferenceDate.publicationDate??>${doc.datasetReferenceDate.publicationDate.year?c}</#if></publicationYear>
  <#if doc.descriptiveKeywords?has_content>
  <subjects>
    <#list doc.descriptiveKeywords as descriptiveKeyword>
    <#list descriptiveKeyword.keywords as keyword>
    <#if keyword.value?has_content>
      <#if keyword.uri?has_content>
      <subject valueURI="${keyword.uri}">${keyword.value}</subject>
      <#else>
      <subject>${keyword.value}</subject>
      </#if>
    </#if>
    </#list>
    </#list>
  </subjects>
  </#if>
  <dates>
    <#setting date_format = 'yyyy-MM-dd'>
    <date dateType="Submitted">${doc.datasetReferenceDate.publicationDate}</date>
    <#if doc.datasetReferenceDate.creationDate?has_content>
      <date dateType="Created">${doc.datasetReferenceDate.creationDate}</date>
    </#if>
    <#if doc.datasetReferenceDate.releasedDate?has_content>
      <date dateType="Available">${doc.datasetReferenceDate.releasedDate}</date>
    </#if>
  </dates>
  <language>en</language>
  <#if resourceType?has_content>
    <resourceType resourceTypeGeneral="${resourceType}"/>
  </#if>
  <#if doc.resourceIdentifiers?has_content>
  <alternateIdentifiers>
    <#list doc.resourceIdentifiers as uri>
      <#if uri.codeSpace!="doi:">
      <#if uri.coupledResource?starts_with("http")>
        <alternateIdentifier alternateIdentifierType="URL">${uri.coupledResource}</alternateIdentifier>
      <#else>
        <alternateIdentifier alternateIdentifierType="URN">${uri.coupledResource}</alternateIdentifier>
      </#if>
      </#if>
    </#list>
  </alternateIdentifiers>
  </#if>
 
  <#include "_related.xml.tpl">
  <#include "_formats.xml.tpl">
  <#include "_rights.xml.tpl">

  <descriptions>
    <description descriptionType="Abstract">${doc.description}</description>
    <#if doc.lineage?has_content && doc.lineage??>
      <description descriptionType="Methods">${doc.lineage}</description>
    </#if>
  </descriptions>
  <#if doc.boundingBoxes?has_content && doc.boundingBoxes??>
  <geoLocations>
    <#list doc.boundingBoxes as extent>
    <geoLocation>
      <geoLocationBox>
        <westBoundLongitude>${extent.westBoundLongitude}</westBoundLongitude>
        <eastBoundLongitude>${extent.eastBoundLongitude}</eastBoundLongitude>
        <southBoundLatitude>${extent.southBoundLatitude}</southBoundLatitude>
        <northBoundLatitude>${extent.northBoundLatitude}</northBoundLatitude>
      </geoLocationBox>
    </geoLocation>
    </#list>
  </geoLocations>
  </#if>
  <#if doc.funding?has_content>
  <fundingReferences>
  <#list doc.funding as funder>
    <fundingReference>
      <funderName>${funder.funderName}</funderName>
        <#if funder.funderIdentifier?has_content>
        <#assign funderIdentifierType="Other">
          <#if funder.funderIdentifier?matches("^http(|s)://(|dx.)doi.org/10.13039/\\d+$")>
              <#assign funderIdentifierType="Crossref Funder">
          <#elseif funder.funderIdentifier?matches("^http(|s)://ror.org/[0-9a-z]+$")>
              <#assign funderIdentifierType="Other">
          </#if>
        <funderIdentifier funderIdentifierType="${funderIdentifierType}">${funder.funderIdentifier}</funderIdentifier>
        </#if>
      <#if funder.awardNumber?has_content>
        <awardNumber>${funder.awardNumber}</awardNumber>
      </#if>
      <#if funder.awardTitle?has_content>
        <awardTitle>${funder.awardTitle}</awardTitle>
      </#if>
    </fundingReference>
  </#list>
  </fundingReferences>
  </#if>
</resource>
</#escape>
</#compress>



