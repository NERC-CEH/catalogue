<#if otherContacts?has_content || distributorContacts?has_content>
  <div id="document-otherContacts">
  <h3><a id="otherContacts"></a>Other contacts</h3>
  <dl class="dl-horizontal">

  <#if otherContacts?has_content>
    <#list otherContacts as otherContact>
      <dt>${otherContact.roleDisplayName?html}</dt>
      <dd>

      <#if otherContact.email?has_content>
        <#if otherContact.individualName?has_content>
          <a href="mailto:${otherContact.email?url}&amp;subject=RE:${title?url}">${otherContact.individualName?html}</a><br>
            <#if otherContact.organisationName?has_content>
              <span>${otherContact.organisationName?html}</span><br>
            </#if>
          <#else>
            <a href="mailto:${otherContact.email?url}&amp;subject=RE:${title?url}">${otherContact.organisationName?html}</a><br>
          </#if>
        <#else>
          <#if otherContact.individualName?has_content>
            <span>${otherContact.individualName?html}</span><br>
          </#if>
          <#if otherContact.organisationName?has_content>
            <span>${otherContact.organisationName?html}</span><br>
          </#if>
        </#if>
                
        <#if otherContact.address?has_content>
          <address class="hidden-xs">
            <#if otherContact.address.deliveryPoint?has_content>${otherContact.address.deliveryPoint?html}<br></#if>
            <#if otherContact.address.city?has_content>${otherContact.address.city?html}<br></#if>
            <#if otherContact.address.administrativeArea?has_content>${otherContact.address.administrativeArea?html}<br></#if>
            <#if otherContact.address.postalCode?has_content>${otherContact.address.postalCode?html}<br></#if>
            <#if otherContact.address.country?has_content>${otherContact.address.country?html}</#if>
          </address>
        </#if>
      </dd>
    </#list>
  </#if>
    
  <#if distributorContacts?has_content>
    <#list distributorContacts as distributorContact>
      <dt>${distributorContact.roleDisplayName?html}</dt>
      <dd>
        <#if distributorContact.email?has_content>
          <#if distributorContact.individualName?has_content>
            <a href="mailto:${distributorContact.email?url}&amp;subject=RE:${title?url}">${distributorContact.individualName?html}</a><br>
            <#if distributorContact.organisationName?has_content>
              <span>${distributorContact.organisationName?html}</span><br>
            </#if>
          <#else>
            <a href="mailto:${distributorContact.email?url}&amp;subject=RE:${title?url}">${distributorContact.organisationName?html}</a><br>
          </#if>
        <#else>
          <#if distributorContact.individualName?has_content>
            <span>${distributorContact.individualName?html}</span><br>
          </#if>
          <#if distributorContact.organisationName?has_content>
            <span>${distributorContact.organisationName?html}</span><br>
          </#if>
        </#if>
                
        <#if distributorContact.address?has_content>
          <address class="hidden-xs">
            <#if distributorContact.address.deliveryPoint?has_content>${distributorContact.address.deliveryPoint?html}<br></#if>
            <#if distributorContact.address.city?has_content>${distributorContact.address.city?html}<br></#if>
            <#if distributorContact.address.administrativeArea?has_content>${distributorContact.address.administrativeArea?html}<br></#if>
            <#if distributorContact.address.postalCode?has_content>${distributorContact.address.postalCode?html}<br></#if>
            <#if distributorContact.address.country?has_content>${distributorContact.address.country?html}</#if>
          </address>
        </#if>
      </dd>
    </#list>
  </#if>
  </dl>
  </div>
</#if>  
