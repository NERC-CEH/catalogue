<#if boundingBoxes?has_content || spatialRepresentationTypes?? || spatialReferenceSystems??>
  <div id="spatial">
  <h2>Spatial information</h2>
    <dl class="dl-horizontal">

      <#if boundingBoxes?has_content>
        <dt>Study area</dt>
        <dd>
          <div id="studyarea-map" class="studyarea-map">
            <#list boundingBoxes as extent>
              <span content="${extent.bounds}" datatype="geoJson"/>
            </#list>
          </div>
        </dd>
      </#if>

      <#if spatialRepresentationTypes?has_content>
        <dt>Spatial representation type<#if spatialRepresentationTypes?size gt 1>s</#if></dt>
        <dd>
          <#list spatialRepresentationTypes as spatialRepresentationType>
            <#assign srLabels=[{"value":"grid", "label":"Raster"},{"value":"stereoModel", "label":"Stereo model"},{"value":"textTable", "label":"Tabular (text)"},{"value":"tin", "label":"Triangular irregular network"},{"value":"video", "label":"Video"},{"value":"vector", "label":"Vector"}]>
            <@getLabel val= spatialRepresentationType array=srLabels/>
            <#if spatialRepresentationType_has_next><br></#if>
          </#list>
        </dd>
      </#if>

      <#if spatialReferenceSystems?has_content>
        <dt>Spatial reference system<#if spatialReferenceSystems?size gt 1>s</#if></dt>
        <dd>
          <#list spatialReferenceSystems as SRS>
            <#if SRS.title?has_content>
                ${SRS.title}
              <#else>
                <#if SRS.codeSpace?has_content>
                  <#if SRS.codeSpace == 'urn:ogc:def:crs:EPSG'>
                    <#assign codeSpace='EPSG'>
                  <#else>
                    <#assign codeSpace=SRS.codeSpace>
                  </#if>
                  ${codeSpace}::${SRS.code}
                <#else>
                  ${SRS.code}
                </#if>
              </#if>
            <#if SRS_has_next><br></#if>
          </#list>
        </dd>
      </#if>

    </dl>
  </div>
</#if>
