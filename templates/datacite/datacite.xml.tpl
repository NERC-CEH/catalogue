<#import "../underscore.tpl" as _>
<#assign authors = _.filter(doc.responsibleParties, _.isAuthor) >
<#compress>
<#escape x as x?xml>
<?xml version="1.0" encoding="UTF-8"?>
<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://datacite.org/schema/kernel-3" xsi:schemaLocation="http://datacite.org/schema/kernel-3 http://schema.datacite.org/meta/kernel-3/metadata.xsd">
  <identifier identifierType="DOI">${doi}</identifier>
  <#if authors?has_content>
  <creators>
  <#list authors as author>
    <#if author.individualName?has_content>
      <creator>
	    <creatorName>${author.individualName}</creatorName>
		<#if author.orcid?has_content>
		<nameIdentifier nameIdentifierScheme="ORCID" schemeURI="http://orcid.org/">${author.orcid}</nameIdentifier>
		</#if>
	  </creator>
    </#if>
  </#list>
  </creators>
  </#if>
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
        <subject>${keyword.value}</subject>
      </#if>
      </#list>
    </#list>
    </subjects>
  </#if>
  <dates>
    <#setting date_format = 'yyyy-MM-dd'>
    <date dateType="Submitted">${submitted?date}</date>
  </dates>
  <language>en</language>
  <#if resourceType?has_content>
    <resourceType resourceTypeGeneral="${resourceType}"/>
  </#if>
  <#if doc.resourceIdentifiers?has_content>
  <alternateIdentifiers>
    <#list doc.resourceIdentifiers as uri>
      <#if uri.codeSpace!="doi:">
        <alternateIdentifier alternateIdentifierType="URN">${uri.coupledResource}</alternateIdentifier>
      </#if>
    </#list>
  </alternateIdentifiers>
  </#if>
  <#if doc.revisionOfIdentifier??>  
    <relatedIdentifiers>
      <relatedIdentifier relatedIdentifierType="DOI" relationType="IsNewVersionOf">10.5285/${doc.revisionOfIdentifier}</relatedIdentifier>
    </relatedIdentifiers>
  </#if>  
  <#if doc.distributionFormats?has_content>
  <formats>
    <#list doc.distributionFormats as format>
    <format>${format.name}</format>
    </#list>
  </formats>
  </#if>
  <#if doc.useConstraints?has_content>
    <rightsList>
      <#list doc.useConstraints as useConstraint>
	    <#if useConstraint.code == "license">
			<#if useConstraint.uri?has_content>
			  <rights rightsURI="${useConstraint.uri}">${useConstraint.value}</rights>
			<#else>
			  <rights>${useConstraint.value}</rights>
			</#if>
		</#if>
      </#list>
    </rightsList>
  </#if>
  <descriptions>
    <description descriptionType="Abstract">${doc.description}</description>
  </descriptions>
  <#if doc.boundingBoxes?has_content && doc.boundingBoxes??>
  <geoLocations>
    <#list doc.boundingBoxes as extent>
      <geoLocation>
        <geoLocationBox>${extent.southBoundLatitude} ${extent.westBoundLongitude} ${extent.northBoundLatitude} ${extent.eastBoundLongitude}</geoLocationBox>
     </geoLocation>
    </#list>
  </geoLocations>
  </#if>
</resource>
</#escape>
</#compress>