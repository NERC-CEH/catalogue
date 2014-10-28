<#setting date_format = 'yyyy-MM-dd'>
<p id="section-dates2">
	<#if datasetReferenceDate.publicationDate??>
		
		Publication date: ${datasetReferenceDate.publicationDate}
	</#if>
	
	<#if datasetReferenceDate.creationDate?? || datasetReferenceDate.revisionDate??>
	<span class="additionalDates">

		<#if datasetReferenceDate.creationDate??>
		<span>created  ${datasetReferenceDate.creationDate}</span>
		</#if>
		
		<#if datasetReferenceDate.revisionDate??>
		<span>revised ${datasetReferenceDate.revisionDate}</span>
		</#if>

	</span>
	</#if>
</p>
