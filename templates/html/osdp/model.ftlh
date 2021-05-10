<#import "osdp.ftlh" as o>
<#import "../blocks.ftlh" as b>

<@o.researchArtifact>
  <#if format?? && format?has_content>
    <@b.key "Format" "Format of dataset">${format}</@b.key>
  </#if>
  <#if version?? && version?has_content>
    <@b.key "Version" "Version of dataset">${version}</@b.key>
  </#if>
</@o.researchArtifact>
