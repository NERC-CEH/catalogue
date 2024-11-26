<#if boundingBoxes?has_content || spatialRepresentationTypes?? || spatialReferenceSystems??>
  <div id="spatial">
  <h2>Spatial information</h2>

      <#if boundingBoxes?has_content>
        <div class="row key-value">
          <div class="col-sm-3 key">Study area</div>
          <div class="col-sm-9 value">
            <div id="studyarea-map" class="studyarea-map">
              <#list boundingBoxes as extent>
                <span content="${extent.bounds}" datatype="geoJson"/>
              </#list>
            </div>
          </div>
        </div>
      </#if>

      <#if spatialRepresentationTypes?has_content>
        <div class="row key-value">
          <div class="col-sm-3 key">Spatial representation type<#if spatialRepresentationTypes?size gt 1>s</#if></div>
          <div class="col-sm-9 value">
            <#list spatialRepresentationTypes as spatialRepresentationType>
              <#assign srLabels=[{"value":"grid", "label":"Raster"},{"value":"stereoModel", "label":"Stereo model"},{"value":"textTable", "label":"Tabular (text)"},{"value":"tin", "label":"Triangular irregular network"},{"value":"video", "label":"Video"},{"value":"vector", "label":"Vector"}]>
              <@getLabel val= spatialRepresentationType array=srLabels/>
              <#if spatialRepresentationType_has_next><br></#if>
            </#list>
          </div>
        </div>
      </#if>

      <#if spatialReferenceSystems?has_content>
        <div class="row key-value">
          <div class="col-sm-3 key">Spatial reference system<#if spatialReferenceSystems?size gt 1>s</#if></div>
          <div class="col-sm-9 value">
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
          </div>
        </div>
      </#if>
  </div>
</#if>
