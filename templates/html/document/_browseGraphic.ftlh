<#if onlineResources??>
 <#assign images = func.filter(onlineResources, "function", "browseGraphic")>
	<#if images?? && images?size gt 0>
		<img src="${images?first.url}" alt="${images?first.name}" class="browseGraphic"/>
	</#if>
</#if>
