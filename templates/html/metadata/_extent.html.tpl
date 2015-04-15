<#if boundingBoxes?? || temporalExtent?? >
  <div id="section-extent">
  <h3 id="extent">Where/When</h3>
    <dl class="dl-horizontal">
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
        <dd>
        <#list temporalExtent as extent>
          <div id="temporal-extent">
            <#if extent.begin?has_content>
              <span class="extentBegin">${extent.begin?date}</span>
            <#else>...
            </#if>

            &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;

            <#if extent.end?has_content>
              <span class="extentEnd">${extent.end?date}</span>
            <#elseif resourceStatus?has_content && resourceStatus == "onGoing">present
            <#else>...
            </#if>
          </div>
        </#list>
        </dd>
      </#if>
    </dl>            
  </div>
</#if>