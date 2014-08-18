<h2>Get the Data</h2>
<#if downloadOrder?has_content>
  <div id="document-ordering">
  <#if downloadOrder.orderUrl?has_content>
    <a id="ordering-url" href="${downloadOrder.orderUrl?trim}" property="dcat:accessURL">Order/Download <span class="external-link"></span></a>
  </#if>
  <#if downloadOrder.supportingDocumentsUrl?has_content>
    <a href="${downloadOrder.supportingDocumentsUrl}" rel="help">Supporting documentation  <span class="external-link"></span></a>
  </#if>
  <#if downloadOrder.licenseUrl?has_content>
    <#if downloadOrder.isOgl()>
      <#assign licenseText>Resource available under an <strong>Open Government Licence</strong></#assign>
    <#else>
      <#assign licenseText>Licence terms and conditions apply</#assign>
    </#if>
    <a id="license" href="${downloadOrder.licenseUrl}" property="dc:rights" rel="license">
      ${licenseText} <span class="external-link"></span>
    </a>
  </#if>
  </div>
</#if>
