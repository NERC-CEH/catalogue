<#if doc.distributionFormats?has_content>
  <formats>
    <#list doc.distributionFormats as format>
    <#if format.type?has_content>
      <format>${format.type} ${format.name}</format>
    <#else>
      <format>${format.name}</format>
    </#if>
    </#list>
  </formats>
</#if>