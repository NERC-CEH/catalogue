<#if resourceIdentifiers?has_content>
  <h3>Dataset identifiers</h3>
  
  <p>
    <#list resourceIdentifiers as uri>
      <#if uri.codeSpace="doi:">
        <a href="http://doi.org/${uri.code?html}">${uri.coupleResource?html}</a>
      <#else>
        ${uri.coupleResource?html}
      </#if>
      <#if uri_has_next><br></#if>
    </#list>
  </p>
</#if>