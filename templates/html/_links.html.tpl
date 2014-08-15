<#if documentLinks?size gt 0 && documentLinks??>
  <#if resourceType.value == 'dataset'>
    <#assign documentLinksTitle="Associated Services">
  <#elseif resourceType.value == 'service'>
    <#assign documentLinksTitle="Associated Datasets">
  </#if>
  <tr>
    <td>${documentLinksTitle}</td>
    <td id="document-links">
      <#list documentLinks as link><#if link_index gt 0>,</#if> <a href="${link.href}">${link.title}</a></#list>
    </td>
  </tr>
</#if>