<#if otherContacts?has_content || distributorContacts?has_content>
  <div id="document-otherContacts">
  <h3><a id="otherContacts"></a>Other contacts</h3>
  <dl class="dl-horizontal">

  <#if otherContacts?has_content>
    <#list otherContacts as otherContact>
      <dt>${otherContact.role}</dt>
      <dd id="otherContact-detail" property="foaf:Agent">

      <#if otherContact.email?has_content>
        <#if otherContact.individualName?has_content>
          <a href="mailto:${otherContact.email}&subject=RE:${title}">${otherContact.individualName}</a><br>
            <#if otherContact.organisationName?has_content>
              <span>${otherContact.organisationName}</span><br>
            </#if>
          <#else>
            <a href="mailto:${otherContact.email}&subject=RE:${title}">${otherContact.organisationName}</a><br>
          </#if>
        <#else>
          <#if otherContact.individualName?has_content>
            <span>${otherContact.individualName}</span><br>
          </#if>
          <#if otherContact.organisationName?has_content>
            <span>${otherContact.organisationName}</span><br>
          </#if>
        </#if>
                
        <#if otherContact.address?has_content>
          <address class="hidden-xs">
            <#if otherContact.address.deliveryPoint?has_content>${otherContact.address.deliveryPoint}<br></#if>
            <#if otherContact.address.city?has_content>${otherContact.address.city}<br></#if>
            <#if otherContact.address.administrativeArea?has_content>${otherContact.address.administrativeArea}<br></#if>
            <#if otherContact.address.postalCode?has_content>${otherContact.address.postalCode}<br></#if>
            <#if otherContact.address.country?has_content>${otherContact.address.country}</#if>
          </address>
        </#if>
      </dd>
    </#list>
  </#if>
    
  <#if distributorContacts?has_content>
    <#list distributorContacts as distributorContact>
      <dt>${distributorContact.role}</dt>
      <dd id="distributorContact-detail" property="dcat:Distribution">
        <#if distributorContact.email?has_content>
          <#if distributorContact.individualName?has_content>
            <a href="mailto:${distributorContact.email}&subject=RE:${title}">${distributorContact.individualName}</a><br>
            <#if distributorContact.organisationName?has_content>
              <span>${distributorContact.organisationName}</span><br>
            </#if>
          <#else>
            <a href="mailto:${distributorContact.email}&subject=RE:${title}">${distributorContact.organisationName}</a><br>
          </#if>
        <#else>
          <#if distributorContact.individualName?has_content>
            <span>${distributorContact.individualName}</span><br>
          </#if>
          <#if distributorContact.organisationName?has_content>
            <span>${distributorContact.organisationName}</span><br>
          </#if>
        </#if>
                
        <#if distributorContact.address?has_content>
          <address class="hidden-xs">
            <#if distributorContact.address.deliveryPoint?has_content>${distributorContact.address.deliveryPoint}<br></#if>
            <#if distributorContact.address.city?has_content>${distributorContact.address.city}<br></#if>
            <#if distributorContact.address.administrativeArea?has_content>${distributorContact.address.administrativeArea}<br></#if>
            <#if distributorContact.address.postalCode?has_content>${distributorContact.address.postalCode}<br></#if>
            <#if distributorContact.address.country?has_content>${distributorContact.address.country}</#if>
          </address>
        </#if>
      </dd>
    </#list>
  </#if>
  </dl>
  </div>
</#if>  
