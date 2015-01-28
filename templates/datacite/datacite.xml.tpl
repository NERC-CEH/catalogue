<#import "../underscore.tpl" as _>
<#assign authors = _.filter(responsibleParties, _.isAuthor) >

<?xml version="1.0" encoding="UTF-8"?>
<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://datacite.org/schema/kernel-3" xsi:schemaLocation="http://datacite.org/schema/kernel-3 http://schema.datacite.org/meta/kernel-3/metadata.xsd">
	<identifier identifierType="DOI">10.5285/${id}</identifier>
	<#if authors?has_content>
	<creators>
	<#list authors as author>
		<#if author.individualName?has_content>
		  <creator><creatorName>${author.individualName?html}</creatorName></creator>
		</#if>
	</#list>
	</creators>
	</#if>
	<titles>
		<title>${title?html}</title>
	</titles>
	<publisher>NERC Environmental Information Data Centre</publisher>
	<publicationYear>
		<#if datasetReferenceDate.publicationDate??>
			${datasetReferenceDate.publicationDate.year?c}
		</#if>
	</publicationYear>
	<#if descriptiveKeywords?has_content>
	  <subjects>
		  <#list descriptiveKeywords as descriptiveKeyword>
		  <#list descriptiveKeyword.keywords as keyword>
			<subject>${keyword.value?html}</subject>
		  </#list>
		</#list>
	  </subjects>
	</#if>
	<dates>
		<#setting date_format = 'yyyy-MM-dd'>
		<date dateType="Submitted">${.now?date}</date>
	</dates>
	<language>en</language>
	<resourceType resourceTypeGeneral="${codes.lookup('metadata.scopeCode',resourceType)}"/>
	<#if resourceIdentifiers?has_content>
	<alternateIdentifiers>
		<#list resourceIdentifiers as uri>
			<#if uri.codeSpace!="doi:">
				<alternateIdentifier alternateIdentifierType="URN">${uri.coupleResource?html}</alternateIdentifier>
			</#if>
		</#list>
	</alternateIdentifiers>
	</#if>
	<#if distributionFormats?has_content>
	<formats>
		<#list distributionFormats as format>
		<format>${format.name?html}</format>
		</#list>
	</formats>
	</#if>
	<#if otherConstraints?has_content>
	<rightsList>
		<#list otherConstraints as otherConstraints>
		<rights>${otherConstraints?html}</rights>
		</#list>
	</rightsList>
	</#if>
	<descriptions>
		<description descriptionType="Abstract">${description?html}</description>
	</descriptions>
	<#if boundingBoxes?has_content && boundingBoxes??>
	<geoLocations>
		<#list boundingBoxes as extent>
			<geoLocation>
				<geoLocationBox>${extent.southBoundLatitude} ${extent.westBoundLongitude} ${extent.northBoundLatitude} ${extent.eastBoundLongitude}</geoLocationBox>
		 </geoLocation>
		</#list>
	</geoLocations>
	</#if>
</resource>
