<div class="container hidden-print clearfix">
  <div class="pull-left hidden-xs">
    <p>View as
      <#if resourceType??>
        <#if ['service', 'series', 'dataset']?seq_contains(resourceType.value) >
          <a href="${uri}.xml?format=gemini" class="label label-default">GEMINI 2.3 (XML)</a>
        <#else>
          <a href="${uri}.xml?format=gemini" class="label label-default">ISO 19115 (XML)</a>
        </#if>
        <a href="${uri}?format=ttl" class="label label-default">RDF (Turtle)</a>
      </#if>
      <a href="${uri}?format=json" class="label label-default">json</a>
    </p>
  </div>
  <div class="pull-right">
    <#if catalogue.id == "eidc">
      <#assign privacy="http://eidc.ceh.ac.uk/policies/privacy" />
    <#else>
      <#assign privacy="https://www.ceh.ac.uk/privacy-notice" />
    </#if>
    <a href="${privacy}" target="_blank" rel="noopener noreferrer">Privacy notice</a>
    <#if catalogue.id != "eidc"> | 
      Hosted by <a href="//www.ceh.ac.uk" target="_blank" rel="noopener noreferrer">UKCEH</a>
    </#if>
  </div>
</div>
