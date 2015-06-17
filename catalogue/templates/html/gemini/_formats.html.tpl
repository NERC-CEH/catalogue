<#if distributionFormats?has_content>
<hr>
	<#if distributionFormats?size = 1>
		<p>Format of the data: 
		<#list distributionFormats as format>
		<span>${format.name?html}</span>
		</#list>
		</p>
	<#else>
		<p><strong>Formats of the data: </strong></p>
		<ul>
		  <#list distributionFormats as format>
			<li>${format.name?html}</li>
		  </#list>
		</ul>
	</#if>
</#if>