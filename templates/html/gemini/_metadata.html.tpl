<#if metadataPointsOfContact?? || metadataDate??>
  <div id="section-metadata">
    <h3>Metadata</h3>
    <dl class="dl-horizontal">
      <dt><span class="element" data-content="Unique identifier for this record" data-trigger="hover" data-placement="right">Record identifier</span></dt>
      <dd>${id?html}</dd>
      <#if metadataPointsOfContact?has_content>
      <dt>Record created by</dt>
        <#list metadataPointsOfContact as metadataPointOfContact>
          <dd>
          <div class="responsibleParty">
            <#if metadataPointOfContact.individualName?has_content>
               <div class="individualName">${metadataPointOfContact.individualName?html}</div>
            </#if>
            <#if metadataPointOfContact.organisationName?has_content>
              <div class="organisationName">${metadataPointOfContact.organisationName?html}</div>
            </#if>

            <#if metadataPointOfContact.address?has_content>
			<address>
              <#if metadataPointOfContact.address.deliveryPoint?has_content>
                ${metadataPointOfContact.address.deliveryPoint?html}<br>
              </#if>
              <#if metadataPointOfContact.address.city?has_content>
                ${metadataPointOfContact.address.city?html}<br>
              </#if>
              <#if metadataPointOfContact.address.administrativeArea?has_content>
                ${metadataPointOfContact.address.administrativeArea?html}<br>
              </#if>
              <#if metadataPointOfContact.address.postalCode?has_content>
                ${metadataPointOfContact.address.postalCode?html}<br>
              </#if>
              <#if metadataPointOfContact.address.country?has_content>
                ${metadataPointOfContact.address.country?html}<br>
              </#if>
			</address>
            </#if>

            <#if metadataPointOfContact.email?has_content>
              <div><a href="mailto:${metadataPointOfContact.email?url}">${metadataPointOfContact.email?html}</a></div>
            </#if>
          </div>
          </dd>
        </#list>
      </#if>  
      <#if metadataDateTime?has_content>  
        <dt>Record last updated</dt>
        <dd>${metadataDateTime?html}</dd>
      </#if>
      
      <#if metadataStandardName?has_content>
      <dt>Metadata standard</dt>
      <dd>
        ${metadataStandardName?html}

        <#if metadataStandardVersion?has_content>
        version ${metadataStandardVersion?html}
        </#if>
      </dd>
      </#if>

    </dl>
  </div>
</#if>