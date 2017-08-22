<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.base>
    <#if title?? || temporalExtent?? || parametersMeasured?? || temporalExtent??>
      <@b.sectionHeading>Basic Information</@b.sectionHeading>
      <#if title?has_content>
        <@b.key "Title" "Name">${title}</@b.key>
      </#if>
      <#if parametersMeasured?has_content>
        <@b.key "Parameters Measured" "Parameters measured in dataset">
          <#list parametersMeasured as parameter>
            <@o.parametersMeasured parameter /> 
          </#list>
        </@b.key>
      </#if>
      <#if temporalExtent?has_content>
        <@b.key "Temporal Extent" "Temporal extent of dataset">
            <@o.temporalExtent temporalExtent /> 
        </@b.key>
      </#if>
    </#if>
</@o.base>