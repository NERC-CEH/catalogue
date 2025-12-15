<#if onlineResources??>
 	<#if browseGraphics?? && browseGraphics?size gt 0>
		<img src="${browseGraphics?first.url}" alt="${browseGraphics?first.name}" class="browseGraphic"/>
	</#if>
</#if>
