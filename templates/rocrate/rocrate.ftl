<#compress>
  <#import "../schema.org/macros.ftl" as m>
  <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
    <#assign docType = "Dataset">
  <#elseif type=='aggregate' || type=='series'>
    <#assign docType = "Series">
  <#elseif type=='model' || type=='software' || type=='computationalNotebook'>
    <#assign docType = "SoftwareSourceCode">
  </#if>

  <#assign fileaccess = fileaccess>
  <#if fileaccess?size gt 0>
    <@m.getPartsData id false ; eidchub, suppDocs, combinedParts>
      <@m.rocrate docType combinedParts/>
    </@m.getPartsData>
  <#else>
    not a valid rocrate document
  </#if>

</#compress>
