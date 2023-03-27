<#compress>
<#import "../functions.ftlh" as func>

<#if type=='dataset' || type=='nonGeographicDataset'>
  <#assign docType = "Dataset">
<#elseif type=='application'>
  <#assign docType = "SoftwareSourceCode">
</#if>

<#if onlineResources?has_content>
  <#assign supDoc = func.filterRegex(func.filter(onlineResources, "function", "information"), "url", "https://data-package.ceh.ac.uk/sd/")>
</#if>

<#if docType?has_content>
{

  "title": "${title}",
  <#if description?has_content>"description": "${description}",</#if>

  <#if authors?has_content>
    "authors":[
      <#list authors as author>
        <#if author.individualName?has_content>
          {
            "firstname": "${author.individualName?keep_before(",")}",
            "surname": "${author.individualName?keep_after(",")}"
          }
        </#if><#sep>,</#sep>
      </#list>
    ]
    ,
  </#if>

  <#if boundingBoxes?? && boundingBoxes?has_content>
    "bbox": {
      "north": "${boundingBoxes?first.northBoundLatitude}",
      "south": "${boundingBoxes?first.southBoundLatitude}",
      "east": "${boundingBoxes?first.eastBoundLongitude}",
      "west": "${boundingBoxes?first.westBoundLongitude}"
      },
  </#if>

  <#if temporalExtents?? && temporalExtents?has_content>
    "bbox": {
      "start": "${temporalExtents?first.begin}",
      "end": "${temporalExtents?first.end}"
      },
  </#if>

  <#if lineage?has_content>"lineage": "${lineage}",</#if>

  <#if supDoc?has_content>
    "docs":[
      <#list supDoc as link>
      {
      "title": "${link.name}",
      "url": "${link.url}"
      }<#sep>,
      </#list>
    ]
  </#if>
}
</#if>
</#compress>