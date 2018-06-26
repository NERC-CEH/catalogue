<div class="container hidden-print clearfix">
  <div class="pull-left hidden-xs">
    <p>View as
      <#if resourceType??>
        <#if ['service', 'series', 'dataset']?seq_contains(resourceType.value) >
          <a href="${uri}.xml?format=gemini" class="label label-default">GEMINI 2.1 (XML)</a>
        <#else>
          <a href="${uri}.xml?format=gemini" class="label label-default">ISO 19115 (XML)</a>
        </#if>
        <a href="${uri}?format=ttl" class="label label-default">RDF (Turtle)</a>
      </#if>
      <a href="${uri}?format=json" class="label label-default">json</a>
    </p>
  </div>
  <#if metadataDateTime?has_content>
    <div class="pull-right">
      <small>Last updated ${metadataDateTime?date}</small>
    </div>
  </#if>
</div>

