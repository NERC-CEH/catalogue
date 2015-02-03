<#if doc.spatialRepresentationTypes?has_content || doc.spatialReferenceSystems?has_content>
  <div id="section-spatial">
    <h3>Spatial</h3>
    <dl>
      <#if doc.spatialRepresentationTypes?has_content>
        <dt>Spatial representation type</dt>
        <dd>
          <#list doc.spatialRepresentationTypes as spatialRepresentationType>
            ${spatialRepresentationType?html}
            <#if spatialRepresentationType_has_next><br></#if>
          </#list>
        </dd>
      </#if>

      <#if doc.spatialReferenceSystems?has_content>
        <dt>Spatial Reference System</dt>
        <dd>
          <#list doc.spatialReferenceSystems as spatialReferenceSystem>
            ${spatialReferenceSystem.title!(spatialReferenceSystem.reference)?html}
            <#if spatialReferenceSystem_has_next><br></#if>  
          </#list>
        </dd>
      </#if>
    </dl>
  </div>
</#if>
