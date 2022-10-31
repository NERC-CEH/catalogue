<div class="footer hidden-print clearfix">
  <div class="container">
    
    <div class="pull-left hidden-xs">
      <p>View as
        <span><a href="${uri}?format=json">json</a></span>
        <#if resourceType??>
          <span><a href="${uri}/datacite">Datacite xml</a></span>
          <span><a href="${uri}.xml?format=gemini">ISO 19115 (XML)</a></span>
          <span><a href="${uri}?format=ttl">RDF (Turtle)</a></span>
        </#if>
      </p>
    </div>
    
    <div class="pull-right" role="contentinfo">
      <#if catalogue.id == "eidc">
        <#assign privacy="https://www.eidc.ac.uk/policies/privacy" />
      <#else>
        <#assign privacy="https://www.ceh.ac.uk/privacy-notice" />
      </#if>
      
      <span><a href="${privacy}" target="_blank" rel="noopener noreferrer">Privacy</a></span>
     <span><a href="//www.ceh.ac.uk" target="_blank" rel="noopener noreferrer">Hosted by UKCEH</a></span>
    </div>
  </div>
</div>
