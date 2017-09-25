<#if datasetReferenceDate??>
  <p id="section-dates2">
    <#if datasetReferenceDate.publicationDate??>
      Publication date: ${datasetReferenceDate.publicationDate?html}
    </#if>
  </p>
</#if>