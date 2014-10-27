<p id="section-dates2">
	<#if datasetReferenceDate.publicationDate??>
		<#setting date_format = 'yyyy-MM-dd'>
		Publication date: ${datasetReferenceDate.publicationDate}
	</#if>
	
	<#if datasetReferenceDate.creationDate?? || datasetReferenceDate.revisionDate??>
	<span class="additionalDates">

		<#if datasetReferenceDate.creationDate??>
		<#setting date_format = 'yyyy-MM-dd'>
		<span>created  ${datasetReferenceDate.creationDate}</span>
		</#if>
		
		<#if datasetReferenceDate.revisionDate??>
		<#setting date_format = 'yyyy-MM-dd'>
		<span>revised ${datasetReferenceDate.revisionDate}</span>
		</#if>

	</span>
	</#if>
</p>

