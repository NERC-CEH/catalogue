<div id="section-dates">
	<h3><a id="metadata"></a>Dates</h3>
	<dl class="dl-horizontal">
		<#if datasetReferenceDate.creationDate??>
		<#setting date_format = 'yyyy-MM-dd'>
		<dt>Creation date</dt>
		<dd property="">${datasetReferenceDate.creationDate}</dd>
		</#if>
		<#if datasetReferenceDate.publicationDate??>
		<#setting date_format = 'yyyy-MM-dd'>
		<dt>Publication date</dt>
		<dd>${datasetReferenceDate.publicationDate}</dd>
		</#if>
		<#if datasetReferenceDate.revisionDate??>
		<#setting date_format = 'yyyy-MM-dd'>
		<dt>Revision date</dt>
		<dd>${datasetReferenceDate.revisionDate}</dd>
		</#if>
	</dl>
</div>
