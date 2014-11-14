<#if authors?has_content>
  <div id="document-authors">
  
  <h3><a id="authors"></a>Authors</h3>
      
    <#list authors as author> 
      <div>

        <#if author.email?has_content>
          <#if author.individualName?has_content>
            <a href="mailto:${author.email}&amp;subject=RE:${title}">${author.individualName}</a><br>
            <#if author.organisationName?has_content>
              <span>${author.organisationName}</span><br>
            </#if>
          <#else>
            <a href="mailto:${author.email}&amp;subject=RE:${title}">${author.organisationName}</a><br>
          </#if>
        <#else>
          <#if author.individualName?has_content>
            <span>${author.individualName}</span><br>
          </#if>
          <#if author.organisationName?has_content>
            <span>${author.organisationName}</span><br>
          </#if>
        </#if>
        
        <#if author.address?has_content>
          <address class="hidden-xs">
            <#if author.address.deliveryPoint?has_content>${author.address.deliveryPoint}<br></#if>
            <#if author.address.city?has_content>${author.address.city}<br></#if>
            <#if author.address.administrativeArea?has_content>${author.address.administrativeArea}<br></#if>
            <#if author.address.postalCode?has_content>${author.address.postalCode}<br></#if>
            <#if author.address.country?has_content>${author.address.country}</#if>
          </address>
        </#if>
      </div>
    </#list>

  </div>
</#if>
