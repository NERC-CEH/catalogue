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
             ${func.displayContact(metadataPointOfContact, true)}
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