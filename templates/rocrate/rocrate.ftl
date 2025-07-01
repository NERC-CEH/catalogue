<#compress>
  <#import "../schema.org/macros.ftl" as m>
  <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
    <#assign docType = "Dataset">
  <#elseif type=='aggregate' || type=='series'>
    <#assign docType = "Series">
  <#elseif type=='application'>
    <#assign docType = "SoftwareSourceCode">
  </#if>

  <#assign fileaccess = filter(downloads, "function", "fileAccess")>
  <#if fileaccess?size gt 0>
    <@m.rocrate docType fileDetails.getDetailsFor(id, false)/>
  <#else>
    not a valid rocrate document
  </#if>

</#compress>

<#function filter listData filterBy value>
  <#local result = []>
  <#list listData as item>
    <#if item[filterBy] == value >
      <#local result = result + [item]>
    </#if>
  </#list>
  <#return result>
</#function>
