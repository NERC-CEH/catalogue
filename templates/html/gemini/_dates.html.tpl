<#if datasetReferenceDate??>
  <p id="section-dates2">
    <#if datasetReferenceDate.publicationDate??>

      Publication date: ${datasetReferenceDate.publicationDate?html}
    </#if>

    <#if datasetReferenceDate.creationDate?? || datasetReferenceDate.revisionDate??>
    <span class="additionalDates">
      (<#if datasetReferenceDate.creationDate??>
        <span>created  ${datasetReferenceDate.creationDate?html}</span>
      </#if>
      <#if datasetReferenceDate.revisionDate??>
        <span>revised ${datasetReferenceDate.revisionDate?html}</span>
      </#if>)
    </span>
    </#if>
  </p>
</#if>