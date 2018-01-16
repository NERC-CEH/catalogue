<#if metadataPointsOfContact?? || metadataDate??>
  <div id="section-metadata">
    <h3>Metadata</h3>
    <dl class="dl-horizontal">
      <#if metadataPointsOfContact?has_content>
      <dt>Information maintained by</dt>
        <#list metadataPointsOfContact as metadataPointOfContact>
          <dd>
          <div class="responsibleParty">
             ${func.displayContact(metadataPointOfContact, false, false)}
          </div>
          </dd>
        </#list>
      </#if>
      <#if metadataDateTime?has_content>
        <dt>Last updated</dt>
        <dd>${metadataDateTime?html}</dd>
      </#if>
    </dl>
  </div>
</#if>