<#if description??>
<div class="description " id="segment-description">
  <div>
    <div class="description-text">
      <#include "_browseGraphic.ftlh">
      <@blocks.linebreaksAndLinks description?trim!"" />
      <#if (resourceType.value != 'aggregate' && resourceType.value != 'collection') && datasetReferenceDate??>
        <#if datasetReferenceDate.publicationDate??>
          <div class="publicationDate">
            Publication date: ${datasetReferenceDate.publicationDate}
          </div>
        </#if>
      </#if>
    </div>
    <#if metadata.catalogue == "eidc">
      <#include "_metrics.ftlh">
    </#if>
  </div>
</div>
</#if>
