<#if otherCitationDetails?has_content> <!-- this should be if doi exists -->
	<div id="section-authorsTop">
	  
	<p><small>  
	<#list responsibleParties as author> 		
		<#if author.role == "Author">
			${author.individualName} , <!-- need to sort out trailing comma-->
		</#if>
	</#list>
	
		<#if datasetReferenceDate.publicationDate??>
			<#setting date_format = 'yyyy-MM-dd'>
			(${datasetReferenceDate.publicationDate?substring(0, 4)})
		</#if>
	.<br>doi:10.5285/${id}
	  
	</small></p>
	
	</div>
</#if>
