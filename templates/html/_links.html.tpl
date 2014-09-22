<#if documentLinks?size gt 0 && documentLinks??>
  <#if resourceType.value == 'dataset'>
    <#assign documentLinksTitle="Associated Services">
  <#elseif resourceType.value == 'service'>
    <#assign documentLinksTitle="Associated Datasets">
  </#if>
  <h3>Links</h3>
    <p>
      <#list documentLinks as link><#if link_index gt 0></p><p></#if> <a href="${link.href}">${link.title}</a></#list>
	</p>
</#if>