<#if lineage?? || dataLevel??>
  <div id="section-quality">
    <h2 id="quality">Provenance &amp; quality</h2>
    <#if dataLevel??>
      <p>This dataset has a data level of ${dataLevel}</p>
    </#if>
    <#if lineage??>
      <p><@blocks.linebreaks lineage!"" /></p>
    </#if>
  </div>
</#if>
