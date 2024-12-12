<#if description??>
<div class="description" id="segment-description">
  <div class="description-text">
    <@blocks.linebreaksAndLinks description?trim!"" />

    <#if resourceType.value != 'aggregate' && resourceType.value != 'collection'>
      <#if datasetReferenceDate??>
        <#if datasetReferenceDate.publicationDate??>
          <div class="publicationDate">
            Publication date: ${datasetReferenceDate.publicationDate}
          </div>
        </#if>
      </#if>
    </#if>

  </div>
  <div class="browseimage">
    <#include "_browseGraphic.ftlh">
  </div>
</div>
</#if>
