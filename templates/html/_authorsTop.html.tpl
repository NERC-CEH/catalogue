<#if authors?has_content>
<div id="section-authorsTop">
	<p><small>  
	
	<#list authors as author>
		${author.individualName}<#if author_has_next>,</#if>
	</#list>

	
	<#if datasetReferenceDate.publicationDate??>
		(${datasetReferenceDate.publicationDate.year?c})
	</#if>

	<#if resourceIdentifiers?has_content>
			<#list resourceIdentifiers as uri>
			<#if uri.codeSpace="doi:" &&  uri.code?length == 44 >
				<br>
				<a href="http://doi.org/${uri.code}">${uri.coupleResource}</a>
			</#if>
			</#list>
	</#if>

	</small></p>	
</div>
</#if>