<#compress>
<#import "../schema.org/macros.ftl" as m>
<#if (type=='dataset' || type=='nonGeographicDataset')>
    <#if resourceStatus?lower_case != "deleted">
      <@m.getPartsData id false ; eidchub, suppDocs, combinedParts>
        <@croissant eidchub/>
      </@m.getPartsData>
    </#if>
<#else>
  not a valid croissant document
</#if>

</#compress>

<#macro croissant files=[]>
  {
  "@context": {
    "@language": "en",
    "@vocab": "https://schema.org/",
    "sc": "https://schema.org/",
    "cr": "http://mlcommons.org/croissant/",
    "rai": "http://mlcommons.org/croissant/RAI/",
    "dct": "http://purl.org/dc/terms/",
    "citeAs": "cr:citeAs",
    "column": "cr:column",
    "conformsTo": "dct:conformsTo",
    "data": {
      "@id": "cr:data",
      "@type": "@json"
    },
    "dataType": {
      "@id": "cr:dataType",
      "@type": "@vocab"
    },
    "examples": {
      "@id": "cr:examples",
      "@type": "@json"
    },
    "extract": "cr:extract",
    "field": "cr:field",
    "fileProperty": "cr:fileProperty",
    "fileObject": "cr:fileObject",
    "fileSet": "cr:fileSet",
    "format": "cr:format",
    "includes": "cr:includes",
    "isLiveDataset": "cr:isLiveDataset",
    "jsonPath": "cr:jsonPath",
    "key": "cr:key",
    "md5": "cr:md5",
    "parentField": "cr:parentField",
    "path": "cr:path",
    "recordSet": "cr:recordSet",
    "references": "cr:references",
    "regex": "cr:regex",
    "repeated": "cr:repeated",
    "replace": "cr:replace",
    "samplingRate": "sc:samplingRate",
    "separator": "cr:separator",
    "source": "cr:source",
    "subField": "cr:subField",
    "transform": "cr:transform"
  },
  "@id": "${id?trim}_croissant",
  "@type": "sc:Dataset", <#--check what if type = model code ?? -->
  "name":<@m.displayLiteral title/>,
  "url": "${uri?trim}",
  "dct:conformsTo": "http://mlcommons.org/croissant/1.0",
  "version":<#if version?has_content><@m.displayLiteral version/><#else>1</#if>
  <#if description?has_content>,"description":<@m.displayLiteral description/></#if>
  <@citeAs/>
  <@creationDate/>
  <@publicationDate/>
  <@listLicences/>
  <@distribution files/>
  <@keywords/>
  <@recordSet/>
  <@listContacts authors "creator"/>
  <@listContacts publishers "publisher"/>
  }
</#macro>

<#macro distribution files>
  <#if files?size gt 0 && files?size lt 60000>
    ,"distribution":[
    <#list files as file>
      <#if file.id?has_content && (file.id?ends_with(".csv") || file.id?ends_with(".parquet"))>
        {
          "@type": "cr:FileObject",
          <#t>"@id": "${file.id}"
          <#if file.encodingFormat?? && file.encodingFormat?has_content>,<#t>"encodingFormat": "${file.encodingFormat}"</#if>
          <#if file.sha256?? && file.sha256?has_content>,<#t>"sha256": "${file.sha256}"</#if>
          <#if file.contentUrl?? && file.contentUrl?has_content>,<#t>"contentUrl": "${file.contentUrl}"</#if>
          <#if file.bytes?? && file.bytes?has_content>,<#t>"contentSize": "${file.bytes?long?c} B"</#if>
        }
      </#if>
      <#sep>,</#sep><#t>
    </#list>
    ]
  </#if>
</#macro>

<#macro citeAs>
<#if datacitable && citation?has_content>
,"citeAs": "@Article{${citation.doi}, doi = '${citation.url}', url = '${citation.url}', author = '${citation.authors?join(' AND ')}', publisher = '${citation.publisher}', title = '${citation.title}', year = '${citation.year?string["0000"]}'}"
</#if>
</#macro>

<#macro listLicences>
  <#if licences?? && licences?has_content>
        ,"license": [
          <#list licences as licence>
            <#if licence.uri?matches("^http[s]?://eidc.ac.uk/licences/ogl.+$")>
              {
                "@type": "CreativeWork",
                "name": "Open Government Licence v3",
                "alternateName":"OGL-UK-3.0",
                "license": "https://spdx.org/licenses/OGL-UK-3.0.html"
              }
            <#else>
              {
                "@type": "CreativeWork",
                <#if licence.value??>"name": "${licence.value}",</#if>
                <#if licence.uri??>"license": "${licence.uri}"</#if>
              }
            </#if><#sep>,</#sep>
          </#list>
        ]
  </#if>
</#macro>

<#macro recordSet>
  <#if fileset?? && fileset?has_content>
    ,"recordSet": [
    <#list fileset as filesetOp>
      <#assign fileName = filesetOp.includes?keep_before_last(".")>
          {
            "@type": "cr:RecordSet",
            "@id": "${fileName}",
            "field": [
              <#if filesetOp.observedProperty?has_content>
                <#list filesetOp.observedProperty as op>
                  <#assign dataType = "sc:Text">
                  <#if op.type == 'integer'>
                    <#assign dataType = "sc:Integer">
                  <#elseif op.type == 'number'>
                    <#assign dataType = "sc:Float">
                  <#elseif op.type == 'date'>
                    <#assign dataType = "sc:Date">
                  <#elseif op.type == 'datetime'>
                    <#assign dataType = "sc:DateTime">
                  </#if>
                  {
                    "@type": "cr:Field",
                    "@id": "${fileName}/${op.value?replace('\\s+', '_', 'r')}",
                    "description": "${op.title}",
                    "dataType": "${dataType}",
                    "source": {
                      "fileObject": { "@id": "${filesetOp.includes}" },
                      "extract": {
                        "column": "${op.value}"
                      }
                    }
                  }
                <#sep>,</#sep></#list>
              </#if>
            ]
          }
      <#sep>,</#sep></#list>
    ]
  </#if>
</#macro>

<#macro creationDate>
  <#if datasetReferenceDate?? && datasetReferenceDate.creationDate?has_content>
    ,"dateCreated":"${datasetReferenceDate.creationDate}"
  </#if>
</#macro>

<#macro publicationDate>
  <#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
    ,"datePublished":"${datasetReferenceDate.publicationDate}"
  </#if>
</#macro>

<#macro keywords>
  <#if allKeywords?? && allKeywords?has_content>
    ,"keywords": [
    <#list allKeywords?sort_by("value")?sort_by("uri") as keyword>
         <#if keyword.value?has_content>
          <@m.displayLiteral keyword.value/>
         </#if>
      <#sep>,
    </#list>
    ]
  </#if>
</#macro>

<#macro listContacts contacts type="contact">
  <#if contacts?? && contacts?has_content>
    ,"${type}": [
        <#list contacts as contact>
          <#assign contactid = "#" + type + contact?index>

          <#if contact.fullName?has_content >
            <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
              <#assign contactid = contact.nameIdentifier>
            </#if>
          <#elseif contact.organisationName?has_content>
            <#if contact.organisationIdentifier?has_content>
              <#assign contactid = contact.organisationIdentifier >
            </#if>
          </#if>

            {
            "@id": "${contactid}",
            <#if contact.fullName?has_content >
              "@type": "Person",
              "name": "${contact.fullName}"
              <#if contact.familyName?has_content>,"familyName": "${contact.familyName}"</#if>
              <#if contact.givenName?has_content>,"givenName": "${contact.givenName}"</#if>
              <#if contact.email?has_content>,"email": "${contact.email}"</#if>
              <#if contact.organisationName?trim?has_content>
                ,"affiliation":{
                  "@type":"Organization",
                  "@id": "${contact.organisationIdentifier}",
                  "name":"${contact.organisationName}"
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
    ]
    </#if>
</#macro>
