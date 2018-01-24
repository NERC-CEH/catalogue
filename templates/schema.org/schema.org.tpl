<#compress>
<#import "../functions.tpl" as func>
<#if useConstraints?has_content>
  <#assign licences = func.filter(useConstraints, "code", "license")>
</#if>
<#if responsibleParties?has_content>
  <#assign  authors = func.filter(responsibleParties, "role", "author")
            publishers = func.filter(responsibleParties, "role", "publisher") >
</#if>
<#if onlineResources?has_content>
  <#assign
    downloads = func.filter(onlineResources, "function", "download") + func.filter(onlineResources, "function", "order")
    infoLinks = func.filter(onlineResources, "function", "information")
  >
</#if>

<#if type=='dataset' || type=='nonGeographicDataset'>
  <#assign docType = "Dataset">
<#elseif type=='aggregate' || type=='series'>
  <#assign docType = "Series">
<#elseif type=='application'>
  <#assign docType = "SoftwareSourceCode">
</#if>

<#if docType?has_content>
{
  "@type":"${docType}",
  "name":"${title}",
  <#if description?has_content>"description":"${description}", </#if>
 
  <#if alternateTitles?has_content>
    "alternateName":[
    <#list alternateTitles as altTitle>
      "${altTitle}"<#sep>,
    </#list>
    ],
  </#if>

  <#if datasetReferenceDate?? && datasetReferenceDate.creationDate?has_content>
    "dateCreated":"${datasetReferenceDate.creationDate}",
  </#if>
  <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
    "datePublished":"${datasetReferenceDate.publicationDate}",
  </#if>

  <#if datacitable?string == "true" && citation?has_content>
    "@id": "${citation.url}",
    "identifier": {
          "@type":"PropertyValue",
          "propertyID": "doi",
          "value": "${citation.url}"
        },
    "url": "${citation.url}",
  <#else>
  "@id": "${uri}",
  "url":"${uri}",
  </#if>

  <#if descriptiveKeywords?has_content>
    <#assign kwlist = [] >
    <#list descriptiveKeywords as descriptiveKeyword>
      <#list descriptiveKeyword.keywords as keyword>
      <#assign kwlist = kwlist + [keyword.value] >
      </#list>
    </#list>
    "keywords": [<#list kwlist as kw>"${kw}"<#sep>,</#list>],
  </#if>

  <#if authors?has_content>
    "creator":[
    <#list authors as author>
    <#if author.individualName?has_content>
      {
      "@type":"Person",
      "name": "${author.individualName}"
      <#if author.organisationName?has_content>
      ,"email": "${author.email}"
      </#if>
      <#if author.nameIdentifier?has_content && author.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
        ,"identifier": {
          "@type":"PropertyValue",
          "propertyID": "orcid",
          "value": "${author.nameIdentifier}"
        }
      </#if>
      <#if author.organisationName?has_content>
        ,"affiliation":{
          "@type":"Organization",
          "name":"${author.organisationName}"
        }
      </#if>
      }
    <#else>
      {
      "@type":"Organization",
      "name": "${author.organisationName}"
      <#if author.organisationName?has_content>
      ,"email": "${author.email}"
      </#if>
      <#if author.nameIdentifier?has_content>
      ,"identifier": "${author.nameIdentifier}"
      </#if>
      }
    </#if>
    <#sep>,
    </#list>
    ]
    ,
  </#if>

  <#if temporalExtents?has_content>
    "temporalCoverage":[
    <#list temporalExtents as temporal>
      <#if temporal.begin?has_content>
        <#assign begin = temporal.begin>
      <#else>
        <#assign begin = "">
      </#if>
      <#if temporal.end?has_content>
        <#assign end = temporal.end>
      <#else>
        <#assign end = "">
      </#if>
      "${begin}/${end}"<#sep>,
    </#list>
    ],
  </#if>


  <#if boundingBoxes?has_content>
   "spatialCoverage": [
    <#list boundingBoxes as extent>
      {
        "@type":"Place",
        "geo":{
          "@type":"GeoShape",
          "box":"${extent.westBoundLongitude} ${extent.southBoundLatitude}, ${extent.eastBoundLongitude} ${extent.northBoundLatitude}"
        }
      }<#sep>,
    </#list>
   ],
  </#if>

  <#if docType == "Dataset" || docType == "SoftwareSourceCode">
    <#if licences?has_content>
      "license":[
      <#list licences as licence>
      {
        "@type":"DigitalDocument",
        "name": "${licence.value}"
        <#if licence.uri?has_content>
        ,"url":"${licence.uri}"
        </#if>
      }<#sep>,
      </#list>
      ],
    </#if>

    <#if downloads?has_content>
      "distribution":[
        <#list downloads as download>
        {
          "@type":"DataDownload",
          "contentUrl":"${download.url}"
        }<#sep>,
        </#list>
      ],
    </#if>
  
  <#if publishers?has_content>
    <#assign publisher = publishers?first>
    "publisher":
        {
        "@type":"Organization","name":"${publisher.organisationName}"
        <#if publisher.organisationName?has_content>
        ,"email": "${publisher.email}"
        </#if>
        },
    </#if>
  "provider" : {"@type":"Organization","name":"NERC Environmental Data Centre"},
  "includedInDataCatalog":{"@type":"DataCatalog", "name":"Environmental Information Data Centre", "alternatename":"EIDC", "url":"https://catalogue.ceh.ac.uk/eidc/documents"},
  </#if>
  "@context":"http://schema.org/"
}
</#if>
</#compress>