<#compress>
  <#import "../schema.org/macros.ftl" as m>
  <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
    <#assign docType = "Dataset">
  <#elseif type=='aggregate' || type=='series'>
    <#assign docType = "Series">
  <#elseif type=='application'>
    <#assign docType = "SoftwareSourceCode">
  </#if>
  <@m.rocrate docType fileDetails.getDetailsFor(id, true)/>
</#compress>
