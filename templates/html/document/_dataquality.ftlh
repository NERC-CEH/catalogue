<#if lineage?? || dataLevel??>
 <#assign displayProvenance = 1>
  <div id="section-quality">
    <h2 id="quality">Provenance &amp; quality <a name="prov"></a></h2>
    <#if dataLevel??>
      <p>This dataset has a data level of ${dataLevel}</p>
    </#if>
    <#if lineage??>
      <div><@blocks.linebreaksAndLinks lineage!"" /></div>
    </#if>
  </div>
</#if>