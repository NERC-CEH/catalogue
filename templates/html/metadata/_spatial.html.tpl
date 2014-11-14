<#if spatialRepresentationTypes?has_content || spatialReferenceSystems?has_content>
  <div id="section-spatial">
    <h3>Spatial</h3>
    <dl>
      <#if spatialRepresentationTypes?has_content>
        <dt>Spatial representation type</dt>
        <dd>
          <#list spatialRepresentationTypes as spatialRepresentationType>
            ${spatialRepresentationType?html}
            <#if spatialRepresentationType_has_next><br></#if>
          </#list>
        </dd>
      </#if>

      <#if spatialReferenceSystems?has_content>
        <dt>Spatial Reference System</dt>
        <dd>
          <#list spatialReferenceSystems as spatialReferenceSystem>
            ${spatialReferenceSystem.title!(spatialReferenceSystem.reference)?html}
            <#if spatialReferenceSystem_has_next><br></#if>  
          </#list>
        </dd>
      </#if>
    </dl>
  </div>
</#if>
