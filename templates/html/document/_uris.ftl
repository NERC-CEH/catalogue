<#if resourceIdentifiers?has_content>
  <h3>Dataset identifiers</h3>
  <p>
    <#list resourceIdentifiers as uri>
      <#if uri.codeSpace="doi:">
        <a href="https://doi.org/${uri.code}">${uri.coupledResource}</a>
      <#else>
        ${uri.coupledResource}
      </#if>
      <#if uri_has_next><br></#if>
    </#list>
  </p>
</#if>