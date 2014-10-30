<#if resourceIdentifiers?has_content>
  <h3>Dataset identifiers</h3>
  
  <p>
    <#list resourceIdentifiers as uri>
      <#if uri.codeSpace="doi:">
        <a href="http://doi.org/${uri.code}">${uri.coupleResource}</a>
      <#else>
        ${uri.coupleResource}
      </#if>
      <#if uri_has_next><br></#if>
    </#list>
  </p>
</#if>