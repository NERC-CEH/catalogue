<div id="section-metadata">
  <h3>Metadata</h3>
  <dl class="dl-horizontal">
    <dt><span class="element" data-content="Unique identifier for this record" data-trigger="hover" data-placement="right">Record identifier</span></dt>
    <dd property="dc:identifier" >${id?html}</dd>
    <#if metadataPointsOfContact?has_content>
    <dt>Record created by</dt>
      <#list metadataPointsOfContact as metadataPointOfContact>
        <dd rel="dc:publisher">
          <#if metadataPointOfContact.individualName?has_content>
            <span property="v:fn">${metadataPointOfContact.individualName?html}</span><br>
          </#if>
          <#if metadataPointOfContact.organisationName?has_content>
            <span property="v:organization-name">${metadataPointOfContact.organisationName?html}</span><br>
          </#if>

          <#if metadataPointOfContact.address?has_content>
            <div rel="v:adr">
              <#if metadataPointOfContact.address.deliveryPoint?has_content>
                <span property="v:street-address">${metadataPointOfContact.address.deliveryPoint?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.city?has_content>
                <span property="v:locality">${metadataPointOfContact.address.city?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.administrativeArea?has_content>
                <span property="v:region">${metadataPointOfContact.address.administrativeArea?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.postalCode?has_content>
                <span property="v:postal-code">${metadataPointOfContact.address.postalCode?html}</span><br>
              </#if>
              <#if metadataPointOfContact.address.country?has_content>
                <span property="v:country-name">${metadataPointOfContact.address.country?html}</span>
              </#if>
            </div>
          </#if>

          <#if metadataPointOfContact.email?has_content>
            <p>
              <a href="mailto:${metadataPointOfContact.email?url}" property="v:email">${metadataPointOfContact.email?html}</a>
            </p>
          </#if>
        </dd>
      </#list>
    </#if>  
        
    <dt>Record last updated</dt>
    <dd>${metadataDate?html}</dd>
    
    <#if metadataStandardName?has_content>
    <dt>Metadata standard</dt>
    <dd>
      ${metadataStandardName?html}
      
      <#if metadataStandardVersion?has_content>
      version ${metadataStandardVersion?html}
      </#if>
    </dd>
    </#if>
    
    <dt> </dt>
    <dd><a href="?format=GeminiWhatever">View complete GEMINI 2 metadata record (xml)</a></dd>
  </dl>
</div>
