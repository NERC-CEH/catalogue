<#if spatialRepresentationTypes?has_content || spatialReferenceSystems?has_content>
  <div id="section-spatial">
    <h3>Spatial</h3>
    <dl class="dl-horizontal">
      <#if spatialRepresentationTypes?has_content>
        <dt>Spatial representation type</dt>
        <dd>
          <#list spatialRepresentationTypes as spatialRepresentationType>
            <#assign srLabels=[{"value":"grid", "label":"Raster"},{"value":"stereoModel", "label":"Stereo model"},{"value":"textTable", "label":"Tabular (text)"},{"value":"tin", "label":"Triangular irregular network"},{"value":"video", "label":"Video"},{"value":"vector", "label":"Vector"}]>
            <@getLabel val= spatialRepresentationType array=srLabels/>
            <#if spatialRepresentationType_has_next><br></#if>
          </#list>
        </dd>
      </#if>

      <#if spatialReferenceSystems?has_content>
        <dt>Spatial reference system</dt>
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
