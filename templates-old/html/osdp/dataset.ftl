<#import "osdp.ftl" as o>
<#import "../blocks.ftl" as b>

<@o.researchArtifact>
  <#if format?? && format?has_content>
    <@b.key "Format" "Format of dataset">${format}</@b.key>
  </#if>
  <#if version?? && version?has_content>
    <@b.key "Version" "Version of dataset">${version}</@b.key>
  </#if>
  <#if parametersMeasured?? && parametersMeasured?has_content>
    <@b.key "Parameters Measured" "Parameters measured in dataset">
      <#list parametersMeasured as parameter>
        <@o.parametersMeasured parameter /> 
      </#list>
    </@b.key>
  </#if>
  <#if boundingBox?? && boundingBox?has_content>
    <@b.key "Bounding Box" "Bounding Box of Dataset"><@o.boundingBox boundingBox /></@b.key>
  </#if>
</@o.researchArtifact>