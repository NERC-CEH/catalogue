<div id="section-metadata">
  <h3>Metadata</h3>
  <dl class="dl-horizontal">
    <dt><span class="element" data-content="Unique identifier for this record" data-trigger="hover" data-placement="right">Record identifier</span></dt>
    <dd property="dc:identifier" >${id}</dd>
    <#if metadataPointsOfContact?has_content>
    <dt>Record created by</dt>
      <#list metadataPointsOfContact as metadataPointOfContact>
        <dd rel="foaf:Agent">
          <#if metadataPointOfContact.individualName?has_content>
            <span property="v:n">${metadataPointOfContact.individualName}</span><br>
          </#if>
          <#if metadataPointOfContact.organisationName?has_content>
            <span property="v:Organization">${metadataPointOfContact.organisationName}</span><br>
          </#if>

          <#if metadataPointOfContact.address?has_content>
            <div rel="v:adr">
              <#if metadataPointOfContact.address.deliveryPoint?has_content>
                <span property="v:street-address">${metadataPointOfContact.address.deliveryPoint}</span><br>
              </#if>
              <#if metadataPointOfContact.address.city?has_content>
                <span property="v:locality">${metadataPointOfContact.address.city}</span><br>
              </#if>
              <#if metadataPointOfContact.address.administrativeArea?has_content>
                <span property="v:region">${metadataPointOfContact.address.administrativeArea}</span><br>
              </#if>
              <#if metadataPointOfContact.address.postalCode?has_content>
                <span property="v:postal-code">${metadataPointOfContact.address.postalCode}</span><br>
              </#if>
              <#if metadataPointOfContact.address.country?has_content>
                <span property="v:country-name">${metadataPointOfContact.address.country}</span>
              </#if>
            </div>
          </#if>

          <#if metadataPointOfContact.email?has_content>
            <p>
              <a href="mailto:${metadataPointOfContact.email}" property="v:email">${metadataPointOfContact.email}</a>
            </p>
          </#if>
        </dd>
      </#list>
    </#if>  
        
    <dt>Record last updated</dt>
    <dd>${metadataDate}</dd>
    
    <#if metadataStandardName?has_content>
    <dt>Metadata standard</dt>
    <dd>
      ${metadataStandardName}
      
      <#if metadataStandardVersion?has_content>
      version ${metadataStandardVersion}
      </#if>
    </dd>
    </#if>
    
    <dt> </dt>
    <dd><a href="?format=GeminiWhatever">View complete GEMINI 2 metadata record (xml)</a></dd>
  </dl>
</div>
