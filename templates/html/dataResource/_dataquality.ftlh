<#if lineage?? || dataLevel??>
  <div id="segment-quality">
    <h2 id="quality">Provenance &amp; quality</h2>
    <#if dataLevel??>
      <p>This dataset has a data level of ${dataLevel}</p>
    </#if>
    <#if lineage??>
      <div><@blocks.linebreaksAndLinks lineage?trim!"" /></div>
    </#if>
  </div>
</#if>
