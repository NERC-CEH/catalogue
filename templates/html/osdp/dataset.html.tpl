<#import "osdp.html.tpl" as o>
<#import "../blocks.html.tpl" as b>

<@o.base>
    <#if title?? || format?? || version?? || parametersMeasured??>
      <@b.sectionHeading>Basic Information</@b.sectionHeading>
      <#if title?has_content>
        <@b.key "Title" "Name">${title}</@b.key>
      </#if>
      <#if format?has_content>
        <@b.key "Format" "Format of dataset">${format}</@b.key>
      </#if>
      <#if version?has_content>
        <@b.key "Version" "Version of dataset">${version}</@b.key>
      </#if>
      <#if parametersMeasured?has_content>
        <@b.key "Parameters Measured" "Parameters measured in dataset">
          <#list parametersMeasured as parameter>
            <@o.parametersMeasured parameter /> 
          </#list>
        </@b.key>
      </#if>
    </#if>
</@o.base>