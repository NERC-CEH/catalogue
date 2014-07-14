<#if documentLinks?size gt 0 && documentLinks??>
  <#if resourceType.value == 'dataset'>
    <#assign documentLinksTitle="Associated Services">
  <#elseif resourceType.value == 'service'>
    <#assign documentLinksTitle="Associated Datasets">
  </#if>

<h3>${documentLinksTitle}</h3>

<ul class="list-inline">
  <#list documentLinks as link>
    <li><a href="${link.href}">${link.title}</a></li>
  </#list>
</ul>
</#if>