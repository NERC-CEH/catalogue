<div id="section-dates">
	<h3><a id="metadata"></a>DATES</h3>
	<dl class="dl-horizontal">
		<#if datasetReferenceDate.creationDate??>
		<dt>Creation date</dt>
		<dd property="">${datasetReferenceDate.creationDate}</dd>
		</#if>
		<#if datasetReferenceDate.publicationDate??>
		<dt>Publication date</dt>
		<dd>${datasetReferenceDate.publicationDate}</dd>
		</#if>
		<#if datasetReferenceDate.revisionDate??>
		<dt>Revision date</dt>
		<dd>${datasetReferenceDate.revisionDate}</dd>
		</#if>
	</dl>
</div>
