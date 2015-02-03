<#if doc.documentLinks?size gt 0 && doc.documentLinks??>
  <#if doc.resourceType == 'dataset'>
    <#assign documentLinksTitle="Services associated with this dataset:">
  <#elseif doc.resourceType == 'service'>
    <#assign documentLinksTitle="Datasets associated with this service:">
  </#if>
  <h4>${documentLinksTitle?html}</h4>
  <#list doc.documentLinks as link>
    <p><a href="${link.href?html}">${link.title?html}</a></p>
  </#list>
</#if>