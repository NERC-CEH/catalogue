<#if temporalExtents?? || temporalResolution?? >
  <div id="temporal">
  <h2>Temporal information</h2>
    <dl class="row text-center">
      <#if temporalExtents?has_content>
        <dt class="col-sm-6">Temporal extent</dt>
        <dd class="col-sm-6" id="section-temporalExtents">
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
        <dt class="col-sm-6">Temporal resolution</dt>
        <dd class="col-sm-6">
          <#list temporalResolution as tr>${tr}<#sep>, </#sep></#list>
        </dd>
      </#if>
    </dl>
  </div>
</#if>
