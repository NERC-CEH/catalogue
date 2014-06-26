<#if responsibleParties?has_content>
  <h3>Contacts</h3>
  <#list responsibleParties as contactsList>
  <div class="row">
  
  	<div class="col-md-4"><b>${contactsList.role}</b></div>
  	<div class="col-md-4">
  			<#if contactsList.individualName?has_content>
	      	${contactsList.individualName}<br>
	      </#if>

	      <#if contactsList.organisationName?has_content>
	      	${contactsList.organisationName}<br>
	      </#if>

	      <#if contactsList.address.deliveryPoint?has_content>
	      	${contactsList.address.deliveryPoint}<br>
	      </#if>

	      <#if contactsList.address.administrativeArea?has_content>
	      	${contactsList.address.administrativeArea}<br>
	      </#if>

	      <#if contactsList.address.city?has_content>
	      	${contactsList.address.city}<br>
	      </#if>

	      <#if contactsList.address.country?has_content>
	      	${contactsList.address.country}<br>
	      </#if>

	      <#if contactsList.address.postalCode?has_content>
	      	${contactsList.address.postalCode}<br>
	      </#if>

	      <#if contactsList.email?has_content>
	      	<a href="mailto:${contactsList.email}">${contactsList.email}<br>
	      </#if><br>
	   </div>	 
  </div>
  </#list>
</#if>