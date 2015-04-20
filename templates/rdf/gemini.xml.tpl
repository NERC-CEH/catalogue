<#setting date_format = 'yyyy-MM-dd'>
<#compress>
<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:vcard="http://www.w3.org/2006/vcard/ns#" xmlns:dcat="http://www.w3.org/ns/dcat#"
xmlns:geo="http://www.opengis.net/ont/geosparql#">
<#escape x as x?xml>
  <dcat:Dataset rdf:about="${uri}">
    <dct:identifier>${id}</dct:identifier>
    <dcat:CatalogRecord rdf:resource="${uri}"/>
    <dcat:landingPage rdf:resource="${uri}"/>
    <dct:type>${codes.lookup('metadata.scopeCode', resourceType)!''}</dct:type>
    <dct:title>${title?xml}</dct:title>
    <dct:description>${description?xml}</dct:description>
	<#if datasetReferenceDate.creationDate?has_content>
	<dct:created>${datasetReferenceDate.creationDate?xml}</dct:created>
	</#if>
	<#if datasetReferenceDate.publicationDate?has_content>
	<dct:available>${datasetReferenceDate.publicationDate?xml}</dct:available>
	</#if>
	<#if datasetReferenceDate.modifiedDate?has_content>
	<dct:modified>${datasetReferenceDate.modifiedDate?xml}</dct:modified>
	</#if>
    <#if citation?has_content>
	 <dct:bibliographicCitation>${citation.authors?join(',')} (${citation.year?string["0000"]}). ${citation.title}. ${citation.publisher}. ${citation.url}</dct:bibliographicCitation>
	</#if>
	<#if citation?has_content>
		<dct:rights>If you reuse this data, you must cite ${citation.authors?join(',')?html} (${citation.year?string["0000"]?html}). ${citation.title?html}. ${citation.publisher?html}. ${citation.url?html}</dct:rights>
		<dct:license>If you reuse this data, you must cite ${citation.authors?join(',')?html} (${citation.year?string["0000"]?html}). ${citation.title?html}. ${citation.publisher?html}. ${citation.url?html}</dct:license>
    </#if>
	<#if useLimitations?has_content>
	  <#list useLimitations as useLimitation>
		<#-- starts_with shortened to catch 'reuse' and 're-use' in the wild -->
		<#if !useLimitation?starts_with("If you re")>
		  <dct:rights>${useLimitation}</dct:rights>
		  <dct:license>${useLimitation}</dct:license>
		</#if>
	  </#list>
	</#if>
    <#if boundingBoxes?has_content && boundingBoxes??>
      <#list boundingBoxes as extent>
      <dct:spatial rdf:datatype="http://www.opengis.net/ont/geosparql#wktLiteral">${extent.wkt}</dct:spatial>
      </#list>
    </#if>
    <#if temporalExtent?has_content>
      <#list temporalExtent as extent>
        <dct:temporal rdf:datatype="http://purl.org/dc/terms/PeriodOfTime">${(extent.begin?date)!''}/${(extent.end?date)!''}</dct:temporal>
      </#list>
    </#if>
    <#if topicCategories?has_content>
      <#list topicCategories as topic>
        <dcat:theme>${topic}</dcat:theme>
      </#list>
    </#if>
    <#if descriptiveKeywords?has_content>
      <#list descriptiveKeywords as descriptiveKeyword>
      <#list descriptiveKeyword.keywords as keyword>
		<#if keyword.uri?has_content>
        <dcat:keyword rdf:resource="${keyword.uri}"/>
		<#else>
        <dcat:keyword>${keyword.value}</dcat:keyword>
		</#if>
      </#list>
      </#list>
    </#if>
    <dcat:Distribution>
      <rdf:Description>
        <dcat:accessURL rdf:resource="${downloadOrder.orderUrl}"/>
        <#if distributionFormats?has_content>
          <#list distributionFormats as format>
		  	<dct:format>
				<dct:IMT>
					<rdf:value>${format.name}</rdf:value>
					<rdfs:label>${format.name}</rdfs:label>
				</dct:IMT>
			</dct:format>
          </#list>
        </#if>
      </rdf:Description>
    </dcat:Distribution>
	<#list responsibleParties as responsibleParty>
      <#if responsibleParty.email?has_content && responsibleParty.role=='pointOfContact'>
      <dcat:contactPoint>
        <rdf:Description>
          <#if responsibleParty.individualName?has_content>
            <vcard:n>${responsibleParty.individualName}</vcard:n>
          </#if>  
          <#if responsibleParty.organisationName?has_content>
            <vcard:organization-name>${responsibleParty.organisationName}</vcard:organization-name>
          </#if>
          <vcard:email>${responsibleParty.email}</vcard:email>
          <#if responsibleParty.address?has_content>
            <vcard:adr>
              <rdf:Description>
                <#if responsibleParty.address.deliveryPoint?has_content><vcard:street-address>${responsibleParty.address.deliveryPoint}</vcard:street-address></#if>
                <#if responsibleParty.address.city?has_content><vcard:locality>${responsibleParty.address.city}</vcard:locality></#if>
                <#if responsibleParty.address.administrativeArea?has_content><vcard:region>${responsibleParty.address.administrativeArea}</vcard:region></#if>
                <#if responsibleParty.address.postalCode?has_content><vcard:postal-code>${responsibleParty.address.postalCode}</vcard:postal-code></#if>
                <#if responsibleParty.address.country?has_content><vcard:country-name>${responsibleParty.address.country}</vcard:country-name></#if>
              </rdf:Description>
            </vcard:adr>
          </#if>
        </rdf:Description>
      </dcat:contactPoint>
      </#if>
	  <#if responsibleParty.email?has_content && responsibleParty.role=='publisher'>
      <dct:publisher>
        <rdf:Description>
          <#if responsibleParty.individualName?has_content>
            <vcard:n>${responsibleParty.individualName}</vcard:n>
          </#if>  
          <#if responsibleParty.organisationName?has_content>
            <vcard:organization-name>${responsibleParty.organisationName}</vcard:organization-name>
          </#if>
          <vcard:email>${responsibleParty.email}</vcard:email>
          <#if responsibleParty.address?has_content>
            <vcard:adr>
              <rdf:Description>
                <#if responsibleParty.address.deliveryPoint?has_content><vcard:street-address>${responsibleParty.address.deliveryPoint}</vcard:street-address></#if>
                <#if responsibleParty.address.city?has_content><vcard:locality>${responsibleParty.address.city}</vcard:locality></#if>
                <#if responsibleParty.address.administrativeArea?has_content><vcard:region>${responsibleParty.address.administrativeArea}</vcard:region></#if>
                <#if responsibleParty.address.postalCode?has_content><vcard:postal-code>${responsibleParty.address.postalCode}</vcard:postal-code></#if>
                <#if responsibleParty.address.country?has_content><vcard:country-name>${responsibleParty.address.country}</vcard:country-name></#if>
              </rdf:Description>
            </vcard:adr>
          </#if>
        </rdf:Description>
      </dct:publisher>
      </#if>
    </#list>
    <foaf:homepage rdf:resource="${uri}"/>
	<dct:language>en</dct:language>
  </dcat:Dataset>
</#escape>
</rdf:RDF>
</#compress>