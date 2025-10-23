<#compress>
  <#import "../schema.org/macros.ftl" as m>
  <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
    <#assign docType = "Dataset">
  <#elseif type=='aggregate' || type=='series'>
    <#assign docType = "Series">
  <#elseif type=='software' || type=='model' || type=='compuationalNotebook'>
    <#assign docType = "SoftwareSourceCode">
  </#if>

  <@m.getPartsData id true ; eidchub, suppDocs, combinedParts>
    <@m.rocrate docType combinedParts/>
  </@m.getPartsData>
</#compress>
