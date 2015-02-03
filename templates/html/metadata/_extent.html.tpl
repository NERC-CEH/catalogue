<#if doc.boundingBoxes?? || doc.temporalExtent?? >
  <div id="section-extent">
  <h3 id="extent">Where/When</h3>
    <dl class="dl-horizontal">
      <#if doc.boundingBoxes?has_content>
        <dt>Study area</dt>
        <dd>
          <div id="studyarea-map">
            <#list doc.boundingBoxes as extent>
              <span property="dc:spatial" content="${extent.wkt?html}" datatype="geo:wktLiteral"/>
            </#list>
          </div>
        </dd>
      </#if>
      <#if doc.temporalExtent?has_content>
        <dt>Temporal extent</dt>
        <dd>
        <#list doc.temporalExtent as extent>
          <div id="temporal-extent" property="dc:temporal" datatype="dc:PeriodOfTime" content="${(extent.begin?date)!''}/${(extent.end?date)!''}">
            <#if extent.begin?has_content>
              <span class="extentBegin">${extent.begin?date}</span>
            <#else>...
            </#if>

            &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;

            <#if extent.end?has_content>
              <span class="extentEnd">${extent.end?date}</span>
            <#elseif doc.resourceStatus?has_content && doc.resourceStatus == "onGoing">present
            <#else>...
            </#if>
          </div>
        </#list>
        </dd>
      </#if>
    </dl>            
  </div>
</#if>