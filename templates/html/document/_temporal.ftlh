<#if temporalExtents?? || temporalResolution?? >
  <div id="temporal">
  <h2>Temporal information</h2>
    <dl class="dl-horizontal">
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

      <#if temporalResolution?has_content>
        <dt>Temporal resolution</dt>
        <dd>
          <#list temporalResolution as tr>${tr}<#sep>, </#sep></#list>
        </dd>
      </#if>
    </dl>
  </div>
</#if>
