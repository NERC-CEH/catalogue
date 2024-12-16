<#if description??>
<div class="description" id="segment-description">
  <div>
    <div class="description-text">
      <#include "_browseGraphic.ftlh">
      <@blocks.linebreaksAndLinks description?trim!"" />
    </div>
    <#if metadata.catalogue == "eidc">
      <#include "_metrics.ftlh">
    </#if>
  </div>
</div>
  <#if resourceType.value != 'aggregate' && resourceType.value != 'collection'>
    <#if datasetReferenceDate??>
      <#if datasetReferenceDate.publicationDate??>
        <div class="publicationDate">
          Publication date: ${datasetReferenceDate.publicationDate}
        </div>
      </#if>
    </#if>
  </#if>
</#if>
