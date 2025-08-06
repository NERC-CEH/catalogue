<#-- Top-level entry macros either 'rocrate' or 'schemaDotOrg' -->

<#macro rocrate docType="" parts=[]>
  <#if docType?has_content>
    {
    "@context": "https://w3id.org/ro/crate/1.2/context",
    "@graph": [
    {
    "@type": "CreativeWork",
    "@id": "ro-crate-metadata.json",
    "conformsTo": { "@id": "https://w3id.org/ro/crate/1.2" },
    "about": { "@id": "${uri?trim}" }
    },
    <@schemaDocument docType parts/>
    ]
    }
  </#if>
</#macro>

<#macro schemaDotOrg docType="", parts=[]>
  <#if docType?has_content>
    {
    "@context":"http://schema.org/",
    "@graph": [
    <@schemaDocument docType parts/>
    ]
    }
  </#if>
</#macro>

<#macro schemaDocument docType parts>
  {
  "@type":<@displayLiteral docType/>,
  "name":<@displayLiteral title/>,
  "@id": "${uri?trim}",
  <@doi/>
  <@partsList parts/>
  <#if resourceStatus?lower_case != "deleted">
    <#if description?has_content>"description":<@displayLiteral description/>,</#if>
    <@alternateTitlesList/>
    <#if resourceStatus == "Available">"isAccessibleForFree": true,</#if>
    <@creationDate/>
    <@publicationDate/>
    <@observedPropertiesList/>
    <@keywordsList/>
    <#if authors?has_content>"creator": [<@contactList authors "creator"/>],</#if>
    <#if pointsOfContact?has_content>"contactPoint": [<@contactList pointsOfContact/>],</#if>
    <@citationList/>
    <@temporalExtentsList/>
    <#if boundingBoxes?has_content>"spatialCoverage": [<@itemList boundingBoxes "bbox"/>],</#if>
    <#if funding?has_content>"funder": [<@itemList funding "fund"/>],</#if>
    <#if docType == "Dataset" || docType == "SoftwareSourceCode">
      <@licencesLink/>
      <#if downloads?has_content>"distribution": [<@itemList downloads "distribution" />],</#if>
      <@publisherLink/>
    </#if>
    "provider" : {"@id":"https://ror.org/04xw4m193"},
    "includedInDataCatalog":{ "@id": "#eidc-dataCatalogue"}
  <#else>
  <#-- information to include if the dataset has been permanently deleted -->
    "description": "This resource is no longer available please contact the Environmental Information Data Centre for more details",
    "creativeWorkStatus": "Deleted"
  </#if>
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
  <@doiDetail/>
  <@licencesDetail/>
  <#if boundingBoxes?has_content>,<@bboxDetails/></#if>
  <#if authors?has_content>,<@contactDetails authors "creator"/></#if>
  <#if pointsOfContact?has_content>,<@contactDetails pointsOfContact/></#if>
  <#if incomingCitations?has_content>,<@citationDetails/></#if>
  <#if funding?has_content>,<@fundDetails/></#if>
  <#if parts?has_content>,<@partDetails parts/></#if>
  <#if downloads?has_content>,<@distributionDetails/></#if>
  <#if authorPointOfContactWithRORs?has_content>,<@organisationRORs/></#if>
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

<#macro bboxDetails>
  <#list boundingBoxes as bbox>
    {
    "@id": "#bbox${bbox?index}",
    "@type":"Place",
    "geo": {
      "@id": "#geoshape${bbox?index}"
      }
    },
    {
      "@id": "#geoshape${bbox?index}",
      "@type": "GeoShape",
      "box":"${bbox.westBoundLongitude} ${bbox.southBoundLatitude}, ${bbox.eastBoundLongitude} ${bbox.northBoundLatitude}"
    }<#sep>,
  </#list>
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

<#macro doi>
  <#if datacitable && citation?has_content>
    "identifier": {"@id": "${citation.url}"},
    "creditText":"${citation.authors?join(', ')} (${citation.year?string["0000"]}). ${citation.title}. ${citation.publisher}. (${codes.lookup('datacite.resourceTypeGeneral',citation.resourceTypeGeneral)}). ${citation.url}",
  <#else>
    "url":"${uri?trim}",
  </#if>
</#macro>

<#macro doiDetail>
  <#if datacitable && citation?has_content>
    ,{
      "@id": "${citation.url}",
      "@type":"PropertyValue",
      "propertyID": "https://registry.identifiers.org/registry/doi",
      "value": "doi:${citation.doi}",
      "url": "${citation.url}"
    }
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

<#macro licencesLink>
  <#if licences?? && licences?has_content>
    <#if licences?first.uri?? && licences?first.uri?has_content>
      <#if licences?first.uri?matches("^http[s]?://eidc.ceh.ac.uk/licences/OGL/plain")>
        "license": {"@id": "#oglLicence"},
      <#else>
        "license": "${licences?first.uri?trim}",
      </#if>
    </#if>
  </#if>
</#macro>

<#macro licencesDetail>
  <#if licences?? && licences?has_content>
    <#if licences?first.uri?? && licences?first.uri?has_content>
      <#if licences?first.uri?matches("^http[s]?://eidc.ceh.ac.uk/licences/OGL.+$")>
        ,{
        "@id": "#oglLicence",
        "@type": "CreativeWork",
        "name": "Open Government Licence v3",
        "alternateName":"OGL-UK-3.0",
        "license": "https://spdx.org/licenses/OGL-UK-3.0.html"
        }
      </#if>
    </#if>
  </#if>
</#macro>

<#macro observedPropertiesList>
  <#if fileset?? && fileset?has_content>
    <#--Combine the observed Properties lists and de-duplicate -->
    <#assign allObservedProperties = [] />
      <#list fileset as fs>
        <#if fs.observedProperty?has_content>
          <#list fs.observedProperty as op>
            <#if ! allObservedProperties?seq_contains(op)>
              <#assign allObservedProperties = allObservedProperties + [ op ] />
            </#if>
          </#list>
        </#if>
      </#list>

    "variableMeasured": [
        <#list allObservedProperties as op>
          <#assign opLabel="">

          <#if op.title?has_content>
            <#assign opLabel=op.title?trim>
          <#elseif op.value?has_content>
            <#assign opLabel=op.value?trim>
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
          <#sep>,</#sep>
        </#list>
      ],
  </#if>
</#macro>

<#macro partsList parts>
  <#if parts?size lt 30000>
    <#list parts>
      "hasPart": [
      <#items as part>
        {"@id":
            <#if part.contentUrl?? && part.contentUrl?has_content>
              "${part.contentUrl}"
            <#else>
              "${part.id}"
            </#if>
        }<#t>
        <#sep>,
      </#items>
      ],
    </#list>
  <#else>
    {"@id": "${id}-files"},
  </#if>
</#macro>

<#macro partDetails parts>
  <#if parts?size lt 30000>
    <#list parts as part>
      <#if part.id?has_content>
        {
        "@id":
            <#if part.contentUrl?? && part.contentUrl?has_content>
              "${part.contentUrl}"
            <#else>
              "${part.id}"
            </#if>
        <#t>,"name": "${part.id}"
        <#if part.type?? && part.type?has_content><#t>,"@type": "${part.type}"</#if>
        <#if part.encodingFormat?? && part.encodingFormat?has_content>,<#t>"encodingFormat": "${part.encodingFormat}"</#if>
        <#if part.sha256?? && part.sha256?has_content>,<#t>"sha256": "${part.sha256}"</#if>
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

<#macro publisherLink>
  <#if publishers?has_content>
    <#assign publisher = publishers?first>
    <#if publisher.organisationIdentifier?has_content>
      "publisher":{"@id":"${publisher.organisationIdentifier}"},
    </#if>
  </#if>
</#macro>

<#macro itemList list idlabel="item">
  <#list list as item>
    {"@id": "#${idlabel}${item?index}"}<#sep>,
  </#list>
</#macro>

<#macro contactList contacts idlabel="contact">
  <#list contacts as contact>
    <#assign contactid = "#" + idlabel + contact?index>

    <#if contact.fullName?has_content>
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

    <#if contact.fullName?has_content>
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
    <#if contact.fullName?has_content>
      "@type": "Person",
      "name": "${contact.fullName}"
      <#if contact.familyName?has_content>, "familyName": "${contact.familyName}"</#if>
      <#if contact.givenName?has_content>, "givenName": "${contact. givenName}"</#if>
      <#if contact.email?has_content>,"email": "${contact.email}"</#if>
      <#if contact.organisationName?has_content>
        ,"affiliation":{
        <#if contact.organisationIdentifier?matches("^https://ror\\.org/\\w{8,10}$")>
          "@id": "${contact.organisationIdentifier}"
        <#else>
          "@type":"Organization",
          "name":"${contact.organisationName}"
          <#if contact.organisationIdentifier?has_content>
            ,"identifier":"${contact.organisationIdentifier}"
          </#if>
        </#if>
        }
      </#if>
    <#else>
      "@type":"Organization"
      <#if contact.organisationName?has_content>,"name": "${contact.organisationName}"</#if>
      <#if contact.email?has_content>,"email": "${contact.email}"</#if>
      <#if contact.organisationIdentifier?has_content>,"identifier":"${contact.organisationIdentifier}"</#if>
    </#if>
    }<#sep>,
  </#list>
</#macro>

<#macro citationList>
  <#if incomingCitations?has_content>
    "@reverse": {
    "citation":[
    <#list incomingCitations as citation>
      <#assign citationid = "#citation" + citation?index>
      <#if citation.url?has_content><#assign citationid = citation.url></#if>
      {"@id": "${citationid}"}<#sep>,</#sep>
    </#list>
    ]
    },
  </#if>
</#macro>

<#macro citationDetails>
  <#list incomingCitations as citation>
    <#assign citationid = "#citation" + citation?index>
    <#if citation.url?has_content><#assign citationid = citation.url></#if>
    {
    "@id": "${citationid}",
    "@type": "CreativeWork"
    <#if citation.description?has_content>,"creditText": <@displayLiteral citation.description/></#if>
    <#if citation.url?has_content>,"url": "${citation.url?trim}"</#if>
    }<#sep>,
  </#list>
</#macro>

<#macro distributionDetails>
  <#list downloads as distribution>
    {
    "@id": "#distribution${distribution?index}",
    "@type":"DataDownload",
    "contentUrl":"${distribution.url}"
    <#if distribution.url?ends_with(".zip")>,"encodingFormat":"application/zip"
    <#elseif distribution.url?ends_with(".csv")>,"encodingFormat":"text/csv"
    <#elseif distribution.url?starts_with("https://data-package.ceh.ac.uk/data/")>,"encodingFormat":"application/zip"
    <#elseif distribution.url?starts_with("https://catalogue.ceh.ac.uk/datastore")>,"encodingFormat":"text/directory"
    </#if>
    }<#sep>,
  </#list>
</#macro>

<#macro fundDetails>
  <#list funding as fund>
    {
    "@id": "#fund${fund?index}",
    "@type":"Organization"
    <#if fund.funderName?? && fund.funderName?has_content>,<#t>"name":"${fund.funderName}"</#if>
    }<#sep>,
  </#list>
</#macro>

<#macro displayLiteral string>
<#--Ensure literals do not contain " characters or line breaks-->
  <#t>"${string?trim?replace("\"","'")?replace("\n"," ")}"
</#macro>

<#macro temporalExtentsList>
  <#if temporalExtents?has_content>
    "temporalCoverage":[
    <#list temporalExtents as temporal>
      "${temporal.begin!""}/${temporal.end!""}"<#sep>,
    </#list>
    ],
  </#if>
</#macro>

<#macro organisationRORs>
  <#list authorPointOfContactWithRORs as contact>
    {
    "@id": "${contact.organisationIdentifier}",
    "@type": "Organization",
    "name": "${contact.organisationName}",
    "identifier": "${contact.organisationIdentifier}"
    }<#sep>,
  </#list>
</#macro>

<#macro getPartsData id isAttached>
  <#assign eidchub = fileDetails.getDetailsFor(id, isAttached, "eidchub")>
  <#assign suppDocs = fileDetails.getDetailsFor(id, isAttached, "supporting-documents")>
  <#assign combinedParts = eidchub + suppDocs>
  <#nested eidchub suppDocs combinedParts>
</#macro>
