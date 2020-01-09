<#if distributionFormats?has_content>
	<#assign formats="" >
	<#if distributionFormats?size = 1>
		<#list distributionFormats as format>
			<#assign formats=formats + format.name>
		</#list>
		<#assign formats="Format of the data: " + formats>
	<#else>
		<#list distributionFormats as format>
			<#assign formats=formats + format.name + ", ">
		</#list>
		<#assign formats="This data is available as " + formats?remove_ending(", ")?replace(",(?!.+,)", " or ", "r")>
	</#if>

	<div class="dataFormats" id="section-formats">
		<p>${formats}</p>
	</div>
</#if>
