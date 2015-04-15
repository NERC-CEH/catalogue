<#setting date_format = 'yyyy-MM-dd'>
<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:dc="http://purl.org/dc/terms/" xmlns:vcard="http://www.w3.org/2006/vcard/ns#" xmlns:dcat="http://www.w3.org/ns/dcat#"
xmlns:geo="http://www.opengis.net/ont/geosparql#">
<#escape x as x?xml>
  <rdf:Description rdf:about="${uri}">
    <dcat:CatalogRecord>${uri}</dcat:CatalogRecord>
    <dc:type>${codes.lookup('metadata.scopeCode', resourceType)!''}</dc:type>
    <dc:title>${title}</dc:title>
    <dc:abstract>${description}</dc:abstract>
    <dc:bibliographicCitation>${citation.authors?join(',')} (${citation.year?string["0000"]}). ${citation.title}. ${citation.publisher}. ${citation.url}</dc:bibliographicCitation>
    
    <#if boundingBoxes?has_content && boundingBoxes??>
      <#list boundingBoxes as extent>
      <dc:spatial rdf:datatype="geo:wktLiteral">${extent.wkt}</dc:spatial>
      </#list>
    </#if>
    <#if temporalExtent?has_content>
      <#list temporalExtent as extent>
        <dc:temporal rdf:datatype="dc:PeriodOfTime">${(extent.begin?date)!''}/${(extent.end?date)!''}</dc:temporal>
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
        <dcat:keyword>${keyword.value}</dcat:keyword>
      </#list>
      </#list>
    </#if>
     
    <dc:identifier>${id}</dc:identifier>
    <dcat:Distribution>
      <rdf:Description>
        <dcat:accessURL rdf:resource="${downloadOrder.orderUrl}"/>
        <#if distributionFormats?has_content>
          <#list distributionFormats as format>
            <dc:format>${format.name}</dc:format>
          </#list>
        </#if>
        <#if useLimitations?has_content>
          <#list useLimitations as useLimitation>
            <#-- starts_with shortened to catch 'reuse' and 're-use' in the wild -->
            <#if !useLimitation?starts_with("If you re")>
              <dc:rights>${useLimitation}</dc:rights>
            </#if>
          </#list>
        </#if>
      </rdf:Description>
    </dcat:Distribution>
    
    <#list metadataPointsOfContact as metadataPointOfContact>
      <#if metadataPointOfContact.email?has_content>
      <dc:publisher>
        <rdf:Description>
        
          <#if metadataPointOfContact.individualName?has_content>
            <vcard:n>${metadataPointOfContact.individualName}</vcard:n>
          </#if>  
          <#if metadataPointOfContact.organisationName?has_content>
            <vcard:organization-name>${metadataPointOfContact.organisationName}</vcard:organization-name>
          </#if>
          <vcard:email>${metadataPointOfContact.email}</vcard:email>
        
          <#if metadataPointOfContact.address?has_content>
            <vcard:adr>
              <rdf:Description>
                <#if metadataPointOfContact.address.deliveryPoint?has_content><vcard:street-address>${metadataPointOfContact.address.deliveryPoint}</vcard:street-address></#if>
                <#if metadataPointOfContact.address.city?has_content><vcard:locality>${metadataPointOfContact.address.city}</vcard:locality></#if>
                <#if metadataPointOfContact.address.administrativeArea?has_content><vcard:region>${metadataPointOfContact.address.administrativeArea}</vcard:region></#if>
                <#if metadataPointOfContact.address.postalCode?has_content><vcard:postal-code>${metadataPointOfContact.address.postalCode}</vcard:postal-code></#if>
                <#if metadataPointOfContact.address.country?has_content><vcard:country-name>${metadataPointOfContact.address.country}</vcard:country-name></#if>
              </rdf:Description>
            </vcard:adr>
          </#if>
        </rdf:Description>
      </dc:publisher>
      </#if>
    </#list>
    
  </rdf:Description>
</#escape>
</rdf:RDF>
