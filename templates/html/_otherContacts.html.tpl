 <!-- **metadata and distributor responsibleParties should not show up in this list -->
	
<#if otherContacts?has_content>
	<div id="document-otherContacts">
	
	<h3><a id="otherContacts"></a>otherContacts</h3>

<div class="alert alert-danger alert-dismissible" role="alert">
<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
This needs some work - metadata point of contact (and possibly distributor) should not be in this list
</div>

	
		<dl class="dl-horizontal">
		<#list otherContacts as otherContact>
		<dt>${otherContact.role}</dt>
			<dd id="otherContact-detail">      

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
		</dl>

	</div>
</#if>	