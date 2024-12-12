<#if temporalExtents?? || temporalResolution?? >
  <div id="segment-temporal">
  <h2>Temporal information</h2>
    <div class="row key-value">
      <#if temporalExtents?has_content>
        <div class="col-sm-3 key">Temporal extent</div>
        <div class="col-sm-9 value">
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
        </div>
      </#if>

      <#if temporalResolution?has_content>
        <div class="col-sm-3 key">Temporal resolution</div>
        <div class="col-sm-9 value">
          <#list temporalResolution as tr>${tr}<#sep>, </#sep></#list>
        </div>
      </#if>
    </div>
  </div>
</#if>
