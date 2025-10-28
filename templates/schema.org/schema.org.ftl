<#compress>
  <#import "macros.ftl" as m>
  <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
    <#assign docType = "Dataset">
  <#elseif type=='aggregate' || type=='series'>
    <#assign docType = "Series">
  <#elseif type=='model' || type=='software' || type=='computationalNotebook'>
    <#assign docType = "SoftwareSourceCode">
  </#if>
  <@m.schemaDotOrg docType/>
</#compress>
