<#if otherContacts?has_content || distributorContacts??>
  <div id="document-otherContacts">
  <h3><a id="otherContacts"></a>Other contacts</h3>
  <dl class="dl-horizontal">

  <#if otherContacts?has_content>
    <#list otherContacts as otherContact>
      <dt>${otherContact.roleDisplayName?html}</dt>
      <dd>
		<div class="responsibleParty">
			<#if otherContact.email?has_content>
				<#if otherContact.individualName?has_content>
					<div class="individualName"><a href="mailto:${otherContact.email?url}&amp;subject=RE: ${title?url}">${otherContact.individualName?html}</a></div>
					<#if otherContact.organisationName?has_content>
						<div class="organisationName">${otherContact.organisationName?html}</div>
					</#if>
				<#else>
					<div class="organisationName"><a href="mailto:${otherContact.email?url}&amp;subject=RE: ${title?url}">${otherContact.organisationName?html}</a></div>
				</#if>
			<#else>
				<#if otherContact.individualName?has_content>
					<div class="individualName">${otherContact.individualName?html}</div>
				</#if>
				<#if otherContact.organisationName?has_content>
					<div class="organisationName">${otherContact.organisationName?html}</div>
				</#if>
			</#if>

			<#if otherContact.address?has_content>
				<address class="hidden-xs">
					<#if otherContact.address.deliveryPoint?has_content><div>${otherContact.address.deliveryPoint?html}</div></#if>
					<#if otherContact.address.city?has_content><div>${otherContact.address.city?html}</div></#if>
					<#if otherContact.address.administrativeArea?has_content><div>${otherContact.address.administrativeArea?html}</div></#if>
					<#if otherContact.address.postalCode?has_content><div>${otherContact.address.postalCode?html}</div></#if>
					<#if otherContact.address.country?has_content><div>${otherContact.address.country?html}</div></#if>
				</address>
			</#if>
		</div>
	 </dd>
    </#list>
  </#if>
    
  <#if distributorContacts?has_content>
    <#list distributorContacts as distributorContact>
      <dt>${distributorContact.roleDisplayName?html}</dt>
      <dd>
        <#if distributorContact.email?has_content>
          <#if distributorContact.individualName?has_content>
             <div class="individualName"><a href="mailto:${distributorContact.email?url}&amp;subject=RE: ${title?url}">${distributorContact.individualName?html}</a></div>
            <#if distributorContact.organisationName?has_content>
              <div class="organisationName">${distributorContact.organisationName?html}</div>
            </#if>
          <#else>
		  <div class="organisationName"><a href="mailto:${distributorContact.email?url}&amp;subject=RE: ${title?url}">${distributorContact.organisationName?html}</a></div>
          </#if>
        <#else>
          <#if distributorContact.individualName?has_content>
             <div class="individualName">${distributorContact.individualName?html}</div>
          </#if>
          <#if distributorContact.organisationName?has_content>
            <div class="organisationName">${distributorContact.organisationName?html}</div>
          </#if>
        </#if>
                
        <#if distributorContact.address?has_content>
          <address class="hidden-xs">
		  <#if distributorContact.address.deliveryPoint?has_content><div>${distributorContact.address.deliveryPoint?html}</div></#if>
		  <#if distributorContact.address.city?has_content><div>${distributorContact.address.city?html}</div></#if>
		  <#if distributorContact.address.administrativeArea?has_content><div>${distributorContact.address.administrativeArea?html}</div></#if>
		  <#if distributorContact.address.postalCode?has_content><div>${distributorContact.address.postalCode?html}</div></#if>
		  <#if distributorContact.address.country?has_content><div>${distributorContact.address.country?html}</div></#if>
          </address>
        </#if>
      </dd>
    </#list>
  </#if>
  </dl>
  </div>
</#if>  
