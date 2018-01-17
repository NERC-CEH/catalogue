<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.researchArtifact>
  <#if medium?? && medium?has_content>
    <@b.key "Medium" "Medium of sample">${medium}</@b.key>
  </#if>
  <#if geometry?? && geometry?has_content>
    <@b.key "Geometry" "Geometry of Sample"><@o.geometry geometry /></@b.key>
  </#if>
</@o.researchArtifact>