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
            <#if metadataPointOfContact.individualName?has_content>
              <span>${metadataPointOfContact.individualName?html}</span><br>
            </#if>
            <#if metadataPointOfContact.organisationName?has_content>
              <span>${metadataPointOfContact.organisationName?html}</span><br>
            </#if>

            <#if metadataPointOfContact.address?has_content>
              <#if metadataPointOfContact.address.deliveryPoint?has_content>
                <span>${metadataPointOfContact.address.deliveryPoint?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.city?has_content>
                <span>${metadataPointOfContact.address.city?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.administrativeArea?has_content>
                <span>${metadataPointOfContact.address.administrativeArea?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.postalCode?has_content>
                <span>${metadataPointOfContact.address.postalCode?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.country?has_content>
                <span>${metadataPointOfContact.address.country?html}</span>
              </#if>
            </#if>

            <#if metadataPointOfContact.email?has_content>
              <p>
                <a href="mailto:${metadataPointOfContact.email?url}">${metadataPointOfContact.email?html}</a>
              </p>
            </#if>
          </dd>
        </#list>
      </#if>  
      <#if metadataDate?has_content>  
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