<#import "../blocks.html.tpl" as b>
<#import "osdp.html.tpl" as o>

<@o.base>
  <#if title?? && role?? && address??>
    <@b.sectionHeading>Basic Information</@b.sectionHeading>
    <#if title?has_content>
      <@b.key "Title" "Name">${title}</@b.key>
    </#if>
    <#if role?has_content>
      <@b.key "Role" "Role of agent">${role}</@b.key>
    </#if>
    <#if address?has_content>
      <@b.key "Address" "">
        <@b.linebreaks address />
      </@b.key>
    </#if>
  </#if>
</@o.base>