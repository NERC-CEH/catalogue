<#if lineage?? || dataLevel??>
  <div id="section-quality">
    <h2 id="quality">Provenance &amp; quality</h2>
    <#if dataLevel??>
      <p>This ${recordType?lower_case} has a data level of <strong>${dataLevel}</strong></p>
    </#if>
    <#if lineage??>
      <p><@blocks.linebreaksAndLinks lineage!"" /></p>
    </#if>
  </div>
</#if>
