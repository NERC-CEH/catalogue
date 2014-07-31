<#if datasetReferenceDate?has_content && datasetReferenceDate??>
<h3>Dataset Dates</h3>
  <div class="dataset-reference-date">
   
  	<#if datasetReferenceDate.creationDate??>
	  Creation date: ${datasetReferenceDate.creationDate}<br />
	</#if>
	<#if datasetReferenceDate.publicationDate??>
	  Publication date: ${datasetReferenceDate.publicationDate}<br />
	</#if>
	<#if datasetReferenceDate.revisionDate??>
	  Revision date: ${datasetReferenceDate.revisionDate}<br />
	</#if>

  </div>
</#if>