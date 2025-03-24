<#compress>
  <#import "../schema.org/macros.ftl" as m>

  <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
      <@croissant fileDetails.getDetailsFor(id, false)/>
  </#if>

  <#macro croissant files=[]>
    <#if resourceStatus?lower_case != "deleted">
      {
      "@context": {
        "@language": "en",
        "@vocab": "https://schema.org/",
        "sc": "https://schema.org/",
        "cr": "http://mlcommons.org/croissant/",
        "rai": "http://mlcommons.org/croissant/RAI/",
        "dct": "http://purl.org/dc/terms/",
        "citeAs": "cr:citeAs",
        "conformsTo": "dct:conformsTo",
        "data": {
          "@id": "cr:data",
          "@type": "@json"
        },
        "dataType": {
          "@id": "cr:dataType",
          "@type": "@vocab"
        },
        "fileProperty": "cr:fileProperty",
        "fileObject": "cr:fileObject",
        "fileSet": "cr:fileSet",
        "format": "cr:format",
        "includes": "cr:includes",
        "jsonPath": "cr:jsonPath",
        "path": "cr:path",
        "recordSet": "cr:recordSet"
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
        <@listContacts authors "creator"/>
        <@listContacts publishers "publisher"/>
      }
    </#if>
  </#macro>

  <#macro distribution files>
    <#if files?size gt 0 && files?size lt 60000>
      ,"distribution":[
          <#-- Default folder FileObject-->
          {
           "@type": "cr:FileObject",
            "@id": "waf",
            "contentUrl": "https://catalogue.ceh.ac.uk/datastore/eidchub/${file.id}",
            "description": "Top level web-accessible folder for this data"
          },
          <#-- Default fileset-->
          {
            "@type": "cr:FileSet",
            "@id": "all-files",
            "containedIn": { "@id": "waf" },
            "description": "All files in this dataset",
            "includes": "*.*"
          },
      <#list files as file>
        <#if file.id?has_content>
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
      <#t>,"citeAs": "@Article{
          <#t>${citation.doi},
          <#t>doi = '${citation.url}',
          <#t>url = '${citation.url}',
          <#t>author = '${citation.authors?join(' AND ')}',
          <#t>publisher = '${citation.publisher}',
          <#t>title = '${citation.title}',
          <#t>year = '${citation.year?string["0000"]}'
      <#t>}"
    </#if>
  </#macro>

  <#macro listLicences>
    <#if licences?? && licences?has_content>
          ,"license": [
            <#list licences as licence>
              <#if licence.uri?matches("^http[s]?://eidc.ceh.ac.uk/licences/OGL.+$")>
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
    <#if allKeywords??>
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
    <#if contacts??>
      ,"${type}": [
          <#list contacts as contact>
            <#assign contactid = "#" + type + contact?index>

            <#if contact.individualName?has_content || contact.fullName?has_content >
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
              <#if contact.individualName?has_content || contact.fullName?has_content >

                <#if contact.fullName?has_content >
                  <#local contactName = contact.fullName >
                <#else>
                  <#local contactName = contact.individualName >
                </#if>

                "@type": "Person",
                "name": "${contactName}"
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

 </#compress>