<div id="segment-metadataQuality" class="clearfix d-print-none metadataQuality metadataQuality-report my-3">
  <#if problems?? && problems?has_content>
  <h2>Metadata quality report</h2>
      <#assign errors = func.filter(problems, "severity", "ERROR")>
      <#assign warnings = func.filter(problems, "severity", "WARNING")>
      <#assign infos = func.filter(problems, "severity", "INFO")>

      <#if errors?? && errors?has_content>
        <div class="metadataQuality_error">
          <div class="row qualityHeader">
            <div class="col-auto me-auto"> <i class="fa-solid fa-triangle-exclamation" > </i> ERRORS</div>
            <div class="col-auto"><small>These should always be fixed</small></div>
          </div>
          <ul class="list-group list-group-flush">
              <#list errors as error>
                  <li class="list-group-item">${error.test}</li>
              </#list>
          </ul>
        </div>
      </#if>
      <#if warnings?? && warnings?has_content>
        <div class="metadataQuality_warning ">
          <div class="row qualityHeader">
            <div class="col-auto me-auto"><i class="fa-solid fa-exclamation" > </i> WARNINGS</div>
            <div class="col-auto"><small>These are usually problems but in some circumstances they <i><b><u>MAY</u></b></i> be acceptable - please check carefully</small></div>
          </div>
          <ul class="list-group list-group-flush">
              <#list warnings as warning>
                  <li class="list-group-item">${warning.test}</li>
              </#list>
          </ul>
        </div>
      </#if>
      <#if infos?? && infos?has_content>
        <div class="metadataQuality_info">
          <div class="row qualityHeader">
            <div class="col-auto me-auto"><i class="fa-solid fa-circle-info"> </i> OTHER ISSUES</div>
            <div class="col-auto"><small>For absolute best practice, these issues should be addressed</small></div>
          </div>
          <ul class="list-group list-group-flush">
              <#list infos as info>
                  <li class="list-group-item">${info.test}</li>
              </#list>
          </ul>
        </div>
      </#if>
  </#if>
</div>
