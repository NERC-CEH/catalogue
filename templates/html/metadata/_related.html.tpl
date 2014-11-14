<#if parentIdentifier?has_content || (documentLinks?size gt 0 && documentLinks??)>
  <h3>Related records</h3>

  <#if parentIdentifier?has_content>
    <p>This resource is part of the series: ${parentIdentifier?html}</p>
  </#if>

  <#if documentLinks?size gt 0 && documentLinks??>
    <#if resourceType == 'dataset'>
    <#assign documentLinksTitle="Associated services:">
    <#elseif resourceType == 'service'>
    <#assign documentLinksTitle="Associated datasets:">
    </#if>

    <h4>${documentLinksTitle?html}</h4>
    <#list documentLinks as link>
      <p><a href="${link.href?html}">${link.title?html}</a></p>
    </#list>
  </#if>
</#if>