<#compress>

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
    "name":"ROCRATE TEST ${title}"
    }
</#if>
</#compress>
