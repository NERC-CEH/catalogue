<#if boundingBoxes?has_content || spatialRepresentationTypes?? || spatialReferenceSystems??>
  <div id="segment-spatial">
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
        <@displaySpatialRepresentationTypes spatialRepresentationTypes/>
      </#if>

      <#if spatialReferenceSystems?has_content>
        <@displaySRS spatialReferenceSystems/>
      </#if>

      <#if spatialResolutions?has_content>
        <@displaySpatialResolution spatialResolutions/>
      </#if>

  </div>
</#if>

<#macro displaySpatialResolution data>
  <#assign uomLabels=[{"value":"urn:ogc:def:uom:EPSG::9102", "label":"degrees"},{"value":"urn:ogc:def:uom:EPSG::9001", "label":"metres"},{"value":"urn:ogc:def:uom:EPSG::9107", "label":"arc-minutes"},{"value":"urn:ogc:def:uom:EPSG::9108", "label":"arc-seconds"}]>
  <div class="row key-value">
    <div class="col-sm-3 key">Spatial resolution<#if data?size gt 1>s</#if></div>
    <div class="col-sm-9 value">
      <#list data as resolution>
        <#if resolution.distance?has_content>
          ${resolution.distance}
          <#if resolution.uom?has_content>
              <@getLabel val=resolution.uom array=uomLabels/>
          <#else>
            <span class="text-body-tertiary">unspecified</span>
          </#if>
        </#if>
        <#sep><br>
        <#--if SRS_has_next><br></#if-->
      </#list>
    </div>
  </div>
</#macro>

<#macro displaySRS data>
  <div class="row key-value">
    <div class="col-sm-3 key">Spatial reference system<#if data?size gt 1>s</#if></div>
    <div class="col-sm-9 value">
      <#list data as SRS>
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
          <#sep><br>
      </#list>
    </div>
  </div>
</#macro>

<#macro displaySpatialRepresentationTypes data>
  <#assign srLabels=[{"value":"grid", "label":"Raster"},{"value":"stereoModel", "label":"Stereo model"},{"value":"textTable", "label":"Tabular (text)"},{"value":"tin", "label":"Triangular irregular network"},{"value":"video", "label":"Video"},{"value":"vector", "label":"Vector"}]>
  <div class="row key-value">
    <div class="col-sm-3 key">Spatial representation type<#if data?size gt 1>s</#if></div>
    <div class="col-sm-9 value">
      <#list data as spatialRepresentationType>
        <@getLabel val= spatialRepresentationType array=srLabels/>
          <#sep><br>
      </#list>
    </div>
  </div>
</#macro>
