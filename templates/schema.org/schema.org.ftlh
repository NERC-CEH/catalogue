<#compress>
<#import "../functions.ftlh" as func>

<#if useConstraints?has_content>
  <#assign licences = func.filter(useConstraints, "code", "license")>
</#if>
<#if responsibleParties?has_content>
  <#assign  authors = func.filter(responsibleParties, "role", "author")
            publishers = func.filter(responsibleParties, "role", "publisher") >
</#if>
<#if onlineResources?has_content>
  <#assign
    infoLinks = func.filter(onlineResources, "function", "information")
  >
</#if>

<#-- COMPILE KEYWORD LIST--->
  <#assign keywordList = [] >

  <#if descriptiveKeywords?has_content>
    <#list descriptiveKeywords as descriptiveKeyword>
      <#list descriptiveKeyword.keywords as keyword>
      <#assign keywordList = keywordList + [keyword] >
      </#list>
    </#list>
  </#if>

  <#if allKeywords?has_content>
    <#list allKeywords as keyword>
        <#assign keywordList = keywordList + [keyword]>
    </#list>
  </#if>

  <#--DE-DUPLICATE KEYWORD LIST -->
  <#assign deDup = [] />
  <#list keywordList as originalList>
    <#if ! deDup?seq_contains(originalList)>
        <#assign deDup = deDup + [originalList] />
    </#if>
  </#list>
  <#assign keywordList = deDup?sort_by("value")?sort_by("uri") />
<#-- END KEYWORD LIST--->

<#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
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

  <#if datacitable?string == "true" && citation?has_content>
    "@id": "${citation.url}",
    "identifier": {
          "@type":"PropertyValue",
          "propertyID": "doi",
          "value": "${citation.url}"
        },
    "url": "${citation.url}",
    "creditText":"${citation.authors?join(', ')} (${citation.year?string["0000"]}). ${citation.title}. ${citation.publisher}. (${codes.lookup('datacite.resourceTypeGeneral',citation.resourceTypeGeneral)}). ${citation.url}",
  <#else>
  "@id": "${uri?trim}",
  "url":"${uri?trim}",
  </#if>

    <#if resourceStatus != "Deleted">
      <#if description?has_content>"description":"${description}", </#if>

      <#if alternateTitles?has_content>
        "alternateName":[
        <#list alternateTitles as altTitle>
          "${altTitle}"<#sep>,</#sep>
        </#list>
        ],
      </#if>

      <#if datasetReferenceDate?? && datasetReferenceDate.creationDate?has_content>
        "dateCreated":"${datasetReferenceDate.creationDate}",
      </#if>
      <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
        "datePublished":"${datasetReferenceDate.publicationDate}",
      </#if>

      <#if keywordsObservedProperty?? && keywordsObservedProperty?has_content>
        "variableMeasured": [ <@displayVariables keywordsObservedProperty/>],
      </#if>

      <#if keywordList?has_content>
      "keywords": [<@displayKeywords keywordList/>],
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
              <#if author.organisationIdentifier?has_content>
              ,"identifier":"${author.organisationIdentifier}"
              </#if>
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
          <#if author.organisationIdentifier?has_content>
          ,"identifier":"${author.organisationIdentifier}"
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
        <#if licences?? && licences?has_content>
          <#if licences?first.uri?? && licences?first.uri?has_content>
            "license": "${licences?first.uri?trim}",
          </#if>
        </#if>

        <#list downloads>
          "distribution":[
            <#items as download>
            {
              "@type":"DataDownload",
              "contentUrl":"${download.url}"
              <#if download.url?ends_with(".zip")>,"encodingFormat":"application/zip"</#if>
              <#if download.url?ends_with(".csv")>,"encodingFormat":"text/csv"</#if>
              <#if download.url?starts_with("https://data-package.ceh.ac.uk/data/")>,"encodingFormat":"application/zip"</#if>
              <#if download.url?starts_with("https://catalogue.ceh.ac.uk/datastore")>,"encodingFormat":"text/directory"</#if>
            }<#sep>,
            </#items>
          ],
        </#list>

      <#if publishers?has_content>
        <#assign publisher = publishers?first>
        "publisher":
            {
            "@type":"Organization","name":"${publisher.organisationName}"
            <#if publisher.organisationIdentifier?has_content>
            ,"identifier":"${publisher.organisationIdentifier}"
            </#if>
            <#if publisher.organisationName?has_content>
            ,"email": "${publisher.email}"
            </#if>
            },
        </#if>
      "provider" : {"@type":"Organization","name":"Environmental Information Data Centre"},
      "includedInDataCatalog":{"@type":"DataCatalog", "name":"Environmental Information Data Centre", "alternatename":"EIDC", "url":"https://catalogue.ceh.ac.uk/eidc/documents"},
      </#if>
    <#else>
      "description": "This resource is no longer available please contact the Environmental Information Data Centre for more details",
      "creativeWorkStatus": "Deleted",
    </#if>
      "@context":"http://schema.org/"
    }
  </#if>
</#compress>


<#-- NEEDS UPDATING  -->
<#-- Parent/child resources  NEED ADDING?
http://schema.org/isPartOf
http://schema.org/hasPart
-->

<#macro displayKeywords keywords>
  <#list keywords as keyword>
      <#if keyword.uri?has_content>

        <#assign subjectScheme="" schemeURI="">
        <#if keyword.uri?matches("^http[s]?://inspire.ec.europa.eu/\\S+$")>
          <#assign subjectScheme="European Union INSPIRE registry" schemeURI="http://inspire.ec.europa.eu/registry/">
        <#elseif keyword.uri?matches("^http[s]?://www.wikidata.org/entity/\\S+$")>
          <#assign subjectScheme="Wikidata" schemeURI="https://www.wikidata.org/">
        <#elseif keyword.uri?matches("^http[s]?://sws.geonames.org/\\S+$")>
          <#assign subjectScheme="Geonames" schemeURI="https://www.geonames.org/">
        <#elseif keyword.uri?matches("^http[s]?://www.eionet.europa.eu/gemet/concept/\\S+$")>
          <#assign subjectScheme="GEMET concepts" schemeURI="https://www.eionet.europa.eu/gemet/">
        </#if>
        {
          "@type": "DefinedTerm",
          "@id": "${keyword.uri?trim}",
          "name": "${keyword.value?trim}"
           <#if subjectScheme?has_content>,"inDefinedTermSet": "${schemeURI}"</#if>
        }
      <#else>
        "${keyword.value?trim}"
      </#if>
      <#sep>,</#sep>
    </#list>
</#macro>

<#macro displayVariables keywords>
  <#list keywords as keyword>
      <#if keyword.uri?has_content>
        {
          "@type": "StatisticalVariable",
          "@id": "${keyword.uri?trim}",
          "name": "${keyword.value?trim}"
        }
      <#else>
        "${keyword.value?trim}"
      </#if>
      <#sep>,</#sep>
    </#list>
</#macro>
