<div id="section-extent">
<h2 id="extent">Where/When</h2>
  <#if boundingBoxes?has_content>
      <dt>Study area</dt>
      <dd>
          <div id="studyarea-map">
              <#list boundingBoxes as extent>
                  <span content="${extent.bounds}" datatype="geoJson"/>
              </#list>
          </div>
      </dd>
  </#if>
  <#if geometries?has_content>
      <dt>Study area</dt>
      <dd>
          <div id="studyarea-map">
              <#list geometries as geometry>
                  <span content="${geometry.geometryString}" datatype="geoJson"/>
              </#list>
          </div>
      </dd>
  </#if>
  <#if lifespan?has_content>
    <p>&nbsp;</p>
    <dl>
      <dt>Lifespan</dt>
      <dd id="temporal-extent">

        <#if lifespan.start?has_content>
          <span class="extentBegin">${lifespan.start?date}</span>
        <#else>...
        </#if>

        &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;

        <#if lifespan.end?has_content>
          <span class="extentEnd">${lifespan.end?date}</span>
        <#else>present
        </#if>
      </dd>
    </dl>
  </#if>
</div>
