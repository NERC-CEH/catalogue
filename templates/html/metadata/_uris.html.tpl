<#if resourceIdentifiers?has_content>
	<h3>Dataset identifiers</h3>
	
	<p>
		<#list resourceIdentifiers as uri>
		
			<#if uri.codeSpace="doi:">
				<a href="http://doi.org/${uri.code}">${uri.coupleResource}</a> <#if uri_has_next><br></#if>
			<#else>
				${uri.coupleResource} <#if uri_has_next><br></#if>
			</#if>
		</#list>
	</p>
</#if>