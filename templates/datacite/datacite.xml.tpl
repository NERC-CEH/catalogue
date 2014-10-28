<#function filter things name value not>
    <#local result = []>
    <#list things as thing>

		<#if not == 0>
		<#if thing[name] == value>
            <#local result = result + [thing]>
        </#if>
		</#if>

		<#if not == 1>
		<#if thing[name] != value>
            <#local result = result + [thing]>
        </#if>
		</#if>

		</#list>
    <#return result>
</#function>
<#assign authors = filter(responsibleParties, "role", "Author", 0) >



<?xml version="1.0" encoding="UTF-8"?>
<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://datacite.org/schema/kernel-3" xsi:schemaLocation="http://datacite.org/schema/kernel-3 http://schema.datacite.org/meta/kernel-3/metadata.xsd">
	<identifier identifierType="DOI">10.5285/${id}</identifier>
	<#if authors?has_content>
	<creators>
	<#list authors as author>
		<#if author.individualName?has_content>
		  <creator><creatorName>${author.individualName}</creatorName></creator>
		</#if>
	</#list>
	</creators>
	</#if>
	<titles>
		<title>${title}</title>
	</titles>
	<publisher>NERC Environmental Information Data Centre</publisher>
	<publicationYear>
		<#if datasetReferenceDate.publicationDate??>
			${datasetReferenceDate.publicationDate.year?c}
		</#if>
	</publicationYear>
	<#if descriptiveKeywords?has_content>
	  <#list descriptiveKeywords as descriptiveKeyword>
		  <subjects>
			  <#list descriptiveKeyword.keywords as keyword>
				<subject>${keyword.value}</subject>
			  </#list>
		  </subjects>
	</#list>
	</#if>
	<dates>
		<#setting date_format = 'yyyy-MM-dd'>
		<date dateType="Submitted">${.now?date}</date>
	</dates>
	<language>en</language>
	<resourceType resourceTypeGeneral="${resourceType}"/>
	<#if resourceIdentifiers?has_content>
	<alternateIdentifiers>
		<#list resourceIdentifiers as uri>
			<#if uri.codeSpace!="doi:">
				<alternateIdentifier alternateIdentifierType="URN">${uri.coupleResource}</alternateIdentifier>
			</#if>
		</#list>
	</alternateIdentifiers>
	</#if>
	<#if distributionFormats?has_content>
	<formats>
		<#list distributionFormats as format>
		<format>${format.name}</format>
		</#list>
	</formats>
	</#if>
	<#if otherConstraints?has_content>
		<rightsList>
			<#list otherConstraints as otherConstraints>
			<rights>${otherConstraints}</rights>
			</#list>
		<rightsList>
	</#if>
	<descriptions>
		<description descriptionType="Abstract">${description}</description>
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
