<#if authors?has_content>
  <div id="document-authors">
  
  <h3 id="authors">Authors</h3>
    <#list authors as author> 
      <div class="responsibleParty">        
		<#if author.email?has_content>
          <#if author.individualName?has_content>
            <div class="individualName"><a href="mailto:${author.email?url}&amp;subject=RE: ${title?url}">${author.individualName?html}</a></div>
            <#if author.organisationName?has_content>
              <div class="organisationName">${author.organisationName?html}</div>
            </#if>
          <#else>
            <div class="organisationName"><a href="mailto:${author.email?url}&amp;subject=RE: ${title?url}">${author.organisationName?html}</a></div>
          </#if>
        <#else>
          <#if author.individualName?has_content>
           <div class="individualName">${author.individualName?html}</div>
          </#if>
          <#if author.organisationName?has_content>
            <div class="organisationName">${author.organisationName?html}</div>
          </#if>
        </#if>
        <#if author.orcid?has_content>
        <div class="orchid"><a href="${author.orcid?ensure_starts_with("http://orcid.org/")}" target="_blank"><img src="\static/img/orcid_16x16.png" alt="ORCID iD icon" title="ORCID iD"> http://orcid.org/${author.orcid?html}</a></div>
        </#if>
        <#if author.address?has_content>
          <div class="postalAddress hidden-xs">
		  <#if author.address.deliveryPoint?has_content><div>${author.address.deliveryPoint?html}</div></#if>
		  <#if author.address.city?has_content><div>${author.address.city?html}</div></#if>
		  <#if author.address.administrativeArea?has_content><div>${author.address.administrativeArea?html}</div></#if>
		  <#if author.address.postalCode?has_content><div>${author.address.postalCode?html}</div></#if>
		  <#if author.address.country?has_content><div>${author.address.country?html}</div></#if>
          </div>
        </#if>
      </div>
    </#list>

  </div>
</#if>
