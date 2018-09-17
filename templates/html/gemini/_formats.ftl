<#if distributionFormats?has_content>
<div class="dataFormats" id="section-formats">
	<#if distributionFormats?size = 1>
		<p>Format of the ${recordType?lower_case} :
		<#list distributionFormats as format>
		<span>${format.name?html}</span>
		</#list>
		</p>
	<#else>
		<p><strong>This ${recordType?html} is available as</strong></p>
		<ul class="list-unstyled">
		  <#list distributionFormats as format>
			<li>${format.name?html}</li>
		  </#list>
		</ul>
	</#if>
</div>
</#if>