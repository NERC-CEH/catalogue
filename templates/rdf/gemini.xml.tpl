<#setting date_format = 'yyyy-MM-dd'>
<#if type=='dataset' || type=='nonGeographicDataset' || type=='series'>
    <#assign rootElement = 'dcat:Dataset'>
<#elseif type=='service'>
    <#assign rootElement = 'dcat:Distribution'>
<#else>
    <#assign rootElement = 'rdf:Description'>
</#if>
<#compress>
<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:vcard="http://www.w3.org/2006/vcard/ns#" xmlns:dcat="http://www.w3.org/ns/dcat#"
xmlns:geo="http://www.opengis.net/ont/geosparql#">
<#escape x as x?xml>
   <${rootElement} rdf:about="${uri}">
   <#if resourceIdentifiers??>
    <#list resourceIdentifiers as resourceIdentifier >
       <dct:identifier>${resourceIdentifier.codeSpace}${resourceIdentifier.code}</dct:identifier>
    </#list>
   </#if>
   <#if type=='dataset' || type=='nonGeographicDataset' || type=='service' || type=='series'>
      <dcat:CatalogRecord rdf:resource="${uri}"/>
      <dcat:landingPage rdf:resource="${uri}"/>
   <#else>
      <foaf:homepage rdf:resource="${uri}"/>
   </#if>
   <#if type=='dataset' || type=='nonGeographicDataset'>
      <dct:type rdf:resource="http://purl.org/dc/dcmitype/Dataset" />
   <#elseif type=='service'>
      <dct:type rdf:resource="http://purl.org/dc/dcmitype/Service" />
   <#elseif type=='series'>
      <dct:type rdf:resource="http://purl.org/dc/dcmitype/Collection" />
   <#else>
      <dct:type>${codes.lookup('metadata.scopeCode', resourceType.value)!''}</dct:type>
   </#if>
   <#if resourceType.uri?has_content>
    <dct:type rdf:resource="${resourceType.uri}"/>
   </#if>
   <dct:title>${title}</dct:title>
   <#if alternateTitles??>
    <#list alternateTitles as altTitle>
       <#if altTitle?has_content>
       <dct:alternative>${altTitle}</dct:alternative>
       </#if>
    </#list>
   </#if>
   <#if description??>
    <dct:description>${description}</dct:description>
   </#if>
   <#if datasetReferenceDate??>
    <#if datasetReferenceDate.creationDate?has_content>
     <dct:created>${datasetReferenceDate.creationDate}</dct:created>
    </#if>
    <#if datasetReferenceDate.publicationDate?has_content>
    <dct:available>${datasetReferenceDate.publicationDate}</dct:available>
    </#if>
    <#if datasetReferenceDate.modifiedDate?has_content>
    <dct:modified>${datasetReferenceDate.modifiedDate}</dct:modified>
    </#if>
   </#if>
    <#if citation?has_content>
      <dct:bibliographicCitation>${citation.authors?join(',')} (${citation.year?string["0000"]}). ${citation.title}. ${citation.publisher}. ${citation.url}</dct:bibliographicCitation>
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
    <#if topicCategories??>
      <#list topicCategories as topic>
        <#if topic.uri?has_content>
          <dct:subject rdf:resource="${topic.uri}" />
        <#elseif topic.value?has_content>
          <dct:subject>${topic.value}</dct:subject>
        </#if>
      </#list>
    </#if>
   <#if type=='dataset' || type=='nonGeographicDataset' || type=='service'>
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
   <#else>
      <#if descriptiveKeywords?has_content>
        <#list descriptiveKeywords as descriptiveKeyword>
        <#list descriptiveKeyword.keywords as keyword>
         <#if keyword.uri?has_content>
         <dct:subject rdf:resource="${keyword.uri}"/>  
         <#else>
         <dct:subject>${keyword.value}</dct:subject>
         </#if>
        </#list>
        </#list>
      </#if>
   </#if>
   <#if type=='dataset' || type=='nonGeographicDataset'>
      <dcat:Distribution>
        <rdf:Description>
		 <#if downloadOrder?? && downloadOrder.orderUrl?has_content>
            <dcat:accessURL rdf:resource="${downloadOrder.orderUrl}"/>
         </#if>
         <#if citation?has_content>
            <dct:rights>If you reuse this data, you must cite ${citation.authors?join(',')?html} (${citation.year?string["0000"]?html}). ${citation.title?html}. ${citation.publisher?html}. ${citation.url?html}</dct:rights>
         </#if>
         <#if useLimitations?has_content>
           <#list useLimitations as useLimitation>
            <#if useLimitation.uri?has_content>
              <dct:license rdf:resource="${useLimitation.uri}"/>
              <dct:rights rdf:resource="${useLimitation.uri}"/>
            <#-- starts_with shortened to catch 'reuse' and 're-use' in the wild -->
            <#elseif !useLimitation.value?starts_with("If you re")>
              <dct:rights>${useLimitation.value}</dct:rights>
            </#if>
           </#list>
         </#if>
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
   <#elseif type=='service'>
   <#if downloadOrder.licenseUrl?has_content>
      <dct:license rdf:resource="${downloadOrder.licenseUrl}"/>
      <dct:rights rdf:resource="${downloadOrder.licenseUrl}"/>
   </#if>

      <#if useLimitations?has_content>
        <#list useLimitations as useLimitation>
         <#if !useLimitation?starts_with("If you re")>
           <dct:rights>${useLimitation}</dct:rights>
         </#if>
        </#list>
      </#if>
      <#list service.containsOperations[0..0] as containsOperation>
      <dcat:accessURL rdf:resource="${containsOperation.url}?service=WMS&amp;request=getCapabilities&amp;"/>
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
      </#list>
   </#if>
   <#list responsibleParties as responsibleParty>
      <#if type=='dataset' || type=='nonGeographicDataset' || type=='service' || type=='series'>
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
      </#if>
      <#if responsibleParty.email?has_content && responsibleParty.role=='author'>
      <dct:contributor>
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
      </dct:contributor>
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
    <#if datasetLanguages??>
      <#list datasetLanguages as datasetLanguage>
        <dct:language>${datasetLanguage}</dct:language>
      </#list>
    </#if>
    <#if associatedResources??>
      <#list associatedResources as associatedResource>
         <#if (type=='dataset' || type = 'nonGeographicDataset') && associatedResource.associationType == 'series'>
            <#if associatedResource.href?has_content>
               <dct:isPartOf rdf:resource="${associatedResource.href}"/>
            </#if>
         <#elseif (type=='dataset' || type = 'nonGeographicDataset') && associatedResource.associationType == 'revisionOf'>
            <#if associatedResource.href?has_content>
               <dct:replaces rdf:resource="${associatedResource.href}"/>
            </#if>
         <#elseif (type=='dataset' || type = 'nonGeographicDataset') && associatedResource.associationType == 'service'>
            <#if associatedResource.href?has_content>
               <dcat:distribution rdf:resource="${associatedResource.href}"/>
            </#if>
         <#elseif type=='series' && associatedResource.associationType == 'dataset'>
            <#if associatedResource.href?has_content>
               <dct:hasPart rdf:resource="${associatedResource.href}"/>
            </#if>
         <#elseif type=='service' && associatedResource.associationType == 'dataset'>
            <#if associatedResource.href?has_content>
               <dct:relation rdf:resource="${associatedResource.href}"/>
            </#if>
         </#if>
      </#list>
   </#if>
   <#if conformanceResults?has_content>
      <#list conformanceResults as conformanceResult>
         <dct:conformsTo>${conformanceResult.title?xml} (${conformanceResult.date?xml} - ${conformanceResult.pass?c})</dct:conformsTo>
      </#list>
   </#if>
  </${rootElement}>
</#escape>
</rdf:RDF>
</#compress>