<#if boundingBoxes?? || temporalExtents?? || geometries?? >
  <div id="section-extent">
  <h2 id="extent">Where/When</h2>
    <dl>

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

      <#if temporalExtents?has_content>
        <dt>Temporal extent</dt>
        <dd id="section-temporalExtents">
          <#list temporalExtents as extent>
            <div class="temporalExtent">
              <#if extent.begin?has_content>
                <span class="extentBegin">${extent.begin?date}</span>
              <#else>
                &hellip;
              </#if>
              &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;
              <#if extent.end?has_content>
                <span class="extentEnd">${extent.end?date}</span>
              <#else>
                &hellip;
              </#if>
            </div>
          </#list>
        </dd>
      </#if>

    </dl>
  </div>
</#if>
