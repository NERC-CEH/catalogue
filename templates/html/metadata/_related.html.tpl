<#if documentLinks?size gt 0 && documentLinks??>
  <#if resourceType == 'dataset'>
    <#assign documentLinksTitle="Services associated with this dataset:">
  <#elseif resourceType == 'service'>
    <#assign documentLinksTitle="Datasets associated with this service:">
  </#if>
  <h4>${documentLinksTitle?html}</h4>
  <#list documentLinks as link>
    <p><a href="${link.href?html}">${link.title?html}</a></p>
  </#list>
</#if>