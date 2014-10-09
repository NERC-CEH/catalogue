<#if documentLinks?size gt 0 && documentLinks??>
  <#if resourceType.value == 'dataset'>
    <#assign documentLinksTitle="Associated Services">
  <#elseif resourceType.value == 'service'>
    <#assign documentLinksTitle="Associated Datasets">
  </#if>
  <h3>Related records</h3>
	 	
	<dl>
		<dt>This resource is part of the series:</dt>
		<dd><a href="#">Land Cover Map 2007</a></dd>
		<dt>Related services:</dt>
		<dd><#list documentLinks as link><#if link_index gt 0></p><p></#if> <a href="${link.href}">${link.title}</a></#list></dd>
	</dl>
	
	
</#if>