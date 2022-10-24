<#if distributionFormats?has_content>
	<div class="dataFormats" id="section-formats">
		<h2>
			Format<#if distributionFormats?size gt 1>s</#if>
		</h2>
		<p>
		<#list distributionFormats as format>
			${format.name}<#sep><br></#sep>
		</#list>
		</p>
	</div>
</#if>