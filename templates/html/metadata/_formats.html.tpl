<#if distributionFormats?has_content>
<hr>
	<#if distributionFormats?size = 1>
		<p>The data are available as 
		<#list distributionFormats as format>
		<span>${format.name?html}</span>
		</#list>
		</p>
	<#else>
		<p><strong>The data are available in the following formats:</strong></p>
		<ul>
		  <#list distributionFormats as format>
			<li>${format.name?html}</li>
		  </#list>
		</ul>
	</#if>
</#if>