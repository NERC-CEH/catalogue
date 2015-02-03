<#if doc.datasetReferenceDate??>
  <p id="section-dates2">
    <#if doc.datasetReferenceDate.publicationDate??>

      Publication date: ${doc.datasetReferenceDate.publicationDate?html}
    </#if>

    <#if doc.datasetReferenceDate.creationDate?? || doc.datasetReferenceDate.revisionDate??>
    <span class="additionalDates">
      (<#if doc.datasetReferenceDate.creationDate??>
        <span>created  ${doc.datasetReferenceDate.creationDate?html}</span>
      </#if>
      <#if doc.datasetReferenceDate.revisionDate??>
        <span>revised ${doc.datasetReferenceDate.revisionDate?html}</span>
      </#if>)
    </span>
    </#if>
  </p>
</#if>