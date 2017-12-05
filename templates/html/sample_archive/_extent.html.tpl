<#if boundingBoxes?? || temporalExtents?? >
  <div id="section-extent">
  <h3 id="extent">Where/When</h3>
    <dl>
    
	  <#if boundingBoxes?has_content>
        <dt>Study area</dt>
        <dd>
          <div id="studyarea-map">
            <#list boundingBoxes as extent>
              <span content="${extent.wkt?html}" datatype="geo:wktLiteral"/>
            </#list>
          </div>
        </dd>
      </#if>
	  
      <#if temporalExtent?has_content>
        <dt>Temporal extent</dt>
        <dd id="section-temporalExtent">
        <div class="temporalExtent">
        <#if temporalExtent.begin?has_content>
              <span class="extentBegin">${temporalExtent.begin?date}</span>
            <#else>&hellip;
            </#if>
            &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;
            <#if temporalExtent.end?has_content>
              <span class="extentEnd">${temporalExtent.end?date}</span>
            <#elseif resourceStatus?has_content && resourceStatus == "onGoing">present
            <#else>&hellip;
        </#if>
        </div>
        </dd>
      </#if>

    </dl>            
  </div>
</#if>