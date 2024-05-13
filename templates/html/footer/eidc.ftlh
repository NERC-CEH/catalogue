<div class="footer hidden-print clearfix">
  <div class="container">

    <div class="pull-left hidden-xs">
     View as
      <span><a href="${uri}?format=json">json</a></span>
        <#if resourceType??>
          <span><a href="${uri}.xml?format=gemini">ISO 19115 (xml)</a></span>
          <span><a href="${uri}?format=ttl">RDF (Turtle)</a></span>
          <#if dataciteMintable || citation??>
            <span><a href="${uri?replace("/id/", "/documents/")}/datacite.xml">Datacite xml</a></span>
          </#if>
      </#if>
    </div>

    <div class="pull-right" role="contentinfo">
      <span><a href="https://eidc.ac.uk/policies/privacy" target="_blank" rel="noopener noreferrer">Privacy</a></span>
      <span><a href="https://www.ceh.ac.uk" target="_blank" rel="noopener noreferrer">Hosted by UKCEH</a></span>
    </div>
  </div>
</div>
