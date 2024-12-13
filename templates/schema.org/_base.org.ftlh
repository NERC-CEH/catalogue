<#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
  <#assign docType = "Dataset">
<#elseif type=='aggregate' || type=='series'>
  <#assign docType = "Series">
<#elseif type=='application'>
  <#assign docType = "SoftwareSourceCode">
</#if>

<#macro schemaDocument showFiles=false>
  <#if showFiles && docType == "Dataset">
    <#if isAttached!false>
      <#assign  parts = fileDetails.getDetailsFor(id, true)>
    <#else>
      <#assign  parts = fileDetails.getDetailsFor(id, false)>
    </#if>
  </#if>
  {
    "@type":<@displayLiteral docType/>,
    "name":<@displayLiteral title/>,
    "@id": "${uri?trim}",
    <@partsList/>
    <@datacite/>
    <#if resourceStatus != "Deleted">
      <#if description?has_content>"description":<@displayLiteral description/>,</#if>
      <@alternateTitlesList/>
      <#if resourceStatus == "Available">"isAccessibleForFree": true,</#if>
      <@creationDate/>
      <@publicationDate/>
      <@observedPropertiesList/>
      <@keywordsList/>
      <#if authors?has_content>"author": [<@contactList authors "author" />],</#if>
      <#if pointsOfContact?has_content>"contactPoint": [<@contactList pointsOfContact  />],</#if>
      <#-- Got to this point -->
      <#--Citations-->
      <#if incomingCitations?has_content>
        "@reverse": {
          "citation":[
            <@citationList incomingCitations />
          ]
        },
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
            <@itemList boundingBoxes "bbox"/>
          ],
      </#if>

      <#if funding?has_content>
        "funder": [
            <@itemList funding "fund"/>
          ],
      </#if>

      <#if docType == "Dataset" || docType == "SoftwareSourceCode">

        <#if licences?? && licences?has_content>
          <#if licences?first.uri?? && licences?first.uri?has_content>
            <#if licences?first.uri?matches("^http[s]?://eidc.ceh.ac.uk/licences/OGL.+$")>
              "license": {"@id": "oglLicence"},
            <#else>
              "license": "${licences?first.uri?trim}",
            </#if>
          </#if>
        </#if>

        <#if downloads?has_content>
          "distribution": [<@itemList downloads "distribution" />],
        </#if>

        <#if publishers?has_content>
          <#assign publisher = publishers?first>
          <#if publisher.organisationIdentifier?has_content>
            "publisher":{"@id":"${publisher.organisationIdentifier}"},
          </#if>
        </#if>
      </#if>

      "provider" : {"@id":"https://ror.org/04xw4m193"},
      "includedInDataCatalog":{ "@id": "#eidc-dataCatalogue"},
    <#else>
      <#-- information to include if the dataset has been permanently deleted -->
      "description": "This resource is no longer available please contact the Environmental Information Data Centre for more details",
      "creativeWorkStatus": "Deleted",
    </#if>
    "@context":"http://schema.org/"
    },
    {
      "@id": "#eidc-dataCatalogue",
      "@type":"DataCatalog",
      "name":"Environmental Information Data Centre",
      "alternateName":"EIDC",
      "url":"https://catalogue.ceh.ac.uk/eidc/documents"
    },
    {
      "@id": "https://ror.org/04xw4m193",
      "@type":"Organization",
      "name":"NERC EDS Environmental Information Data Centre",
      "email": "info@eidc.ac.uk"
    }

    <#if licences?? && licences?has_content>
      <#if licences?first.uri?? && licences?first.uri?has_content>
      <#if licences?first.uri?matches("^http[s]?://eidc.ceh.ac.uk/licences/OGL.+$")>
        ,{
            "@id": "oglLicence",
            "@type": "CreativeWork",
            "name": "Open Government Licence v3",
            "alternateName":"OGL-UK-3.0",
            "license": "https://spdx.org/licenses/OGL-UK-3.0.html"
          }
        </#if>
      </#if>
    </#if>

    <#if boundingBoxes?has_content>
      ,<@bboxDetails boundingBoxes />
    </#if>

    <#if authors?has_content>
      ,<@contactDetails authors "author" />
    </#if>

    <#if pocs?has_content>
      ,<@contactDetails pocs />
    </#if>

    <#if incomingCitations?has_content>
      ,<@citationDetails incomingCitations />
    </#if>

    <#if funding?has_content>
      ,<@fundDetails funding />
    </#if>

    <#if parts?? && parts?size gt 0>
      ,<@partDetails parts/>
    </#if>

    <#if downloads?has_content>
      ,<@distributionDetails downloads />
    </#if>
</#macro>

<#macro alternateTitlesList>
  <#if alternateTitles??>
    <#list alternateTitles>
      "alternateName":[
      <#items as altTitle>
        <@displayLiteral altTitle/><#sep>,</#sep>
      </#items>
      ],
    </#list>
  </#if>
</#macro>

<#macro creationDate>
  <#if datasetReferenceDate?? && datasetReferenceDate.creationDate?has_content>
    "dateCreated":"${datasetReferenceDate.creationDate}",
  </#if>
</#macro>

<#macro publicationDate>
  <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
    "datePublished":"${datasetReferenceDate.publicationDate}",
  </#if>
</#macro>

<#macro datacite>
  <#if datacitable && citation?has_content>
    "identifier": {
          "@type":"PropertyValue",
          "propertyID": "https://registry.identifiers.org/registry/doi",
          "value": "doi:${citation.doi}",
          "url": "${citation.url}"
        },
    "url": "${citation.url}",
    "creditText":"${citation.authors?join(', ')} (${citation.year?string["0000"]}). ${citation.title}. ${citation.publisher}. (${codes.lookup('datacite.resourceTypeGeneral',citation.resourceTypeGeneral)}). ${citation.url}",
  <#else>
    "url":"${uri?trim}",
  </#if>
</#macro>

<#macro keywordsList>
  <#if allKeywords??>
    <#list allKeywords?sort_by("value")?sort_by("uri")>
      "keywords": [
      <#items as keyword>
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
          <#t>{
             <#t>"@type": "DefinedTerm",
             <#t>"@id": "${keyword.uri?trim}",
             <#t>"name": <@displayLiteral keyword.value/>
             <#t><#if subjectScheme?has_content>,"inDefinedTermSet": "${schemeURI}"</#if>
          }
        <#else>
          "${keyword.value?trim}"
        </#if>
        <#sep>,
      </#items>],
    </#list>
  </#if>
</#macro>

<#macro observedPropertiesList>
<#if observedProperty?? && observedProperty?has_content>
"variableMeasured": [
  <#list observedProperty as op>
      <#assign opLabel ="unknown">
      <#if op.title?has_content>
        <#assign opLabel = op.title?trim>
      <#elseif op.value?has_content>
        <#assign opLabel = op.value?trim>
      </#if>

      <#if op.uri?has_content>
        {
          "@type": "StatisticalVariable",
          "@id": "${op.uri?trim}",
          "name": "${opLabel}"
          <#if op.unitsUri?has_content>,"unitCode": "${op.unitsUri?trim}"</#if>
          <#if op.units?has_content>,"unitText": "${op.units?trim}"</#if>
        }
      <#else>
        <@displayLiteral opLabel/>
      </#if>
      <#sep>,
    </#list>
    ],
</#if>
</#macro>

<#macro partsList>
  <#if parts?? && parts?size lt 60000>
    <#list parts>
      "hasPart": [
      <#items as part>
        <#if part.id?has_content>{"@id": "${part.id}"}</#if><#sep>,
      </#items>
      ],
    </#list>
  <#else>
    {"@id": "${id}-files"},
  </#if>
</#macro>

<#macro partDetails parts>
  <#if parts?size lt 60000>
    <#list parts as part>
      <#if part.id?has_content>
        {
          <#t>"@id": "${part.id}",
          <#t>"name": "${part.id}"
          <#if part.type?? && part.type?has_content><#t>,"@type": "${part.type}"</#if>
          <#if part.encodingFormat?? && part.encodingFormat?has_content>,<#t>"encodingFormat": "${part.encodingFormat}"</#if>
          <#if part.lastModified?? && part.lastModified?has_content>,<#t>"lastModified": "${part.lastModified}"</#if>
          <#if part.bytes?? && part.bytes?has_content>,<#t>"bytes": ${part.bytes?long?c}</#if>
          <#if part.contentUrl?? && part.contentUrl?has_content>,<#t>"contentUrl": "${part.contentUrl}"</#if>
        }
      </#if>
      <#sep>,</#sep><#t>
    </#list>
  <#else>
    {
    "@id": "${id}-files",
    "@type": "Dataset",
    "name": "Files",
    "description": "This dataset contains ${parts?size} files"
    }
  </#if>
</#macro>

<#macro itemList list idlabel="item">
  <#list list as item>
    <#assign itemid = "#" + idlabel + item?index>
    {"@id": "${itemid}"}<#sep>,</#sep>
  </#list>
</#macro>

<#macro contactList contacts idlabel="contact">
  <#list contacts as contact>
    <#assign contactid = "#" + idlabel + contact?index>
    <#if contact.individualName?has_content>
      <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
        <#assign contactid = contact.nameIdentifier>
      </#if>
    <#elseif contact.organisationName?has_content>
      <#if contact.organisationIdentifier?has_content>
        <#assign contactid = contact.organisationIdentifier >
      </#if>
    </#if>
    {"@id": "${contactid}"}<#sep>,</#sep>
  </#list>
</#macro>

<#macro contactDetails contacts idlabel="contact">
  <#list contacts as contact>
      <#assign contactid = "#" + idlabel + contact?index>
      <#if contact.individualName?has_content>
        <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
          <#assign contactid = contact.nameIdentifier>
        </#if>
      <#elseif contact.organisationName?has_content>
        <#if contact.organisationIdentifier?has_content>
          <#assign contactid = contact.organisationIdentifier>
        </#if>
      </#if>

      {
        "@id": "${contactid}",
        <#if contact.individualName?has_content>
          "@type": "Person",
          "name": "${contact.individualName}"
          <#if contact.email?has_content>,"email": "${contact.email}"</#if>
          <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
            ,"identifier": {
              "@type":"PropertyValue",
              "propertyID": "orcid",
              "value": "${contact.nameIdentifier}"
            }
          </#if>
          <#if contact.organisationName?has_content>
            ,"affiliation":{
              "@type":"Organization",
              "name":"${contact.organisationName}"
              <#if contact.organisationIdentifier?has_content>
              ,"identifier":"${contact.organisationIdentifier}"
              </#if>
            }
          </#if>
        <#else>
          "@type":"Organization"
          <#if contact.organisationName?has_content>,"name": "${contact.organisationName}"</#if>
          <#if contact.email?has_content>,"email": "${contact.email}"</#if>
          <#if contact.organisationIdentifier?has_content>,"identifier":"${contact.organisationIdentifier}"</#if>
        </#if>
        }<#sep>,</#sep>
  </#list>
</#macro>

<#macro citationList citations>
  <#list incomingCitations as citation>
    <#assign citationid = "#citation" + citation?index>
    <#if citation.url?has_content><#assign citationid = citation.url></#if>
    {"@id": "${citationid}"}<#sep>,</#sep>
  </#list>
</#macro>

<#macro citationDetails citations>
  <#list incomingCitations as citation>
    <#assign citationid = "#citation" + citation?index>
    <#if citation.url?has_content><#assign citationid = citation.url></#if>
    {
    "@id": "${citationid}",
    "@type": "CreativeWork"
    <#if citation.description?has_content>,"creditText": <@displayLiteral citation.description/></#if>
    <#if citation.url?has_content>,"url": "${citation.url?trim}"</#if>
    }<#sep>,</#sep>
  </#list>
</#macro>

<#macro bboxDetails boundingBoxes>
  <#list boundingBoxes as bbox>
    <#assign bboxid = "#bbox" + bbox?index>
    {
    "@id": "${bboxid}",
     "@type":"Place",
     "geo":{
        "@type":"GeoShape",
         "box":"${bbox.westBoundLongitude} ${bbox.southBoundLatitude}, ${bbox.eastBoundLongitude} ${bbox.northBoundLatitude}"
          }
    }<#sep>,</#sep>
  </#list>
</#macro>

<#macro distributionDetails distributions>
  <#list downloads as distribution>
    <#assign distributionid = "#distribution" + distribution?index>
    {
    "@id": "${distributionid}",
    "@type":"DataDownload",
    "contentUrl":"${distribution.url}"
    <#if distribution.url?ends_with(".zip")>,"encodingFormat":"application/zip"
    <#elseif distribution.url?ends_with(".csv")>,"encodingFormat":"text/csv"
    <#elseif distribution.url?starts_with("https://data-package.ceh.ac.uk/data/")>,"encodingFormat":"application/zip"
    <#elseif distribution.url?starts_with("https://catalogue.ceh.ac.uk/datastore")>,"encodingFormat":"text/directory"
    </#if>
    }<#sep>,</#sep>
  </#list>
</#macro>

<#macro fundDetails funding>
  <#list funding as fund>
    <#assign fundid = "#fund" + fund?index>
    {
    "@id": "${fundid}",
    "@type":"Organization"
    <#if fund.funderName?? && fund.funderName?has_content>,<#t>"name":"${fund.funderName}" </#if>
    }<#sep>,</#sep>
  </#list>
</#macro>

<#macro displayLiteral string>
  <#--Ensure literals do not contain " characters or line breaks-->
  <#t>"${string?trim?replace("\"","'")?replace("\n"," ")}"
</#macro>
