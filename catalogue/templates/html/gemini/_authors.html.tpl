<#if authors?has_content>
  <div id="document-authors">
  
  <h3 id="authors">Authors</h3>
      
    <#list authors as author> 
      <div>

        <#if author.email?has_content>
          <#if author.individualName?has_content>
            <a href="mailto:${author.email?url}&amp;subject=RE: ${title?url}">${author.individualName?html}</a><br>
            <#if author.organisationName?has_content>
              <span>${author.organisationName?html}</span><br>
            </#if>
          <#else>
            <a href="mailto:${author.email?url}&amp;subject=RE: ${title?url}">${author.organisationName?html}</a><br>
          </#if>
        <#else>
          <#if author.individualName?has_content>
            <span>${author.individualName?html}</span><br>
          </#if>
          <#if author.organisationName?has_content>
            <span>${author.organisationName?html}</span><br>
          </#if>
        </#if>
        <#if author.orcid?has_content>
        <a href="${author.orcid?ensure_starts_with("http://orcid.org/")}" target="_blank">${author.orcid?html}</a><br>
        </#if>
        <#if author.address?has_content>
          <address class="hidden-xs">
            <#if author.address.deliveryPoint?has_content>${author.address.deliveryPoint?html}<br></#if>
            <#if author.address.city?has_content>${author.address.city?html}<br></#if>
            <#if author.address.administrativeArea?has_content>${author.address.administrativeArea?html}<br></#if>
            <#if author.address.postalCode?has_content>${author.address.postalCode?html}<br></#if>
            <#if author.address.country?has_content>${author.address.country?html}</#if>
          </address>
        </#if>
      </div>
    </#list>

  </div>
</#if>
