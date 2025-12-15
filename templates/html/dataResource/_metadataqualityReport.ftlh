<div id="segment-metadataQuality" class="clearfix d-print-none metadataQuality metadataQuality-report my-3">
  <#if problems?? && problems?has_content>
  <h2>Metadata quality report</h2>
      <#if errorList?? && errorList?has_content>
        <div class="metadataQuality_error">
          <div class="qualityHeader d-flex justify-content-between">
            <div> <i class="fa-solid fa-triangle-exclamation" > </i> ERRORS</div>
            <div><small>These should always be fixed</small></div>
          </div>
          <ul class="list-group list-group-flush">
              <#list errorList as error>
                  <li class="list-group-item">${error.test}</li>
              </#list>
          </ul>
        </div>
      </#if>
      <#if warningList?? && warningList?has_content>
        <div class="metadataQuality_warning ">
          <div class="qualityHeader d-flex justify-content-between">
            <div><i class="fa-solid fa-exclamation" > </i> WARNINGS</div>
            <div><small>These are usually problems but in some circumstances they <i><b><u>MAY</u></b></i> be acceptable - please check carefully</small></div>
          </div>
          <ul class="list-group list-group-flush">
              <#list warningList as warning>
                  <li class="list-group-item">${warning.test}</li>
              </#list>
          </ul>
        </div>
      </#if>
      <#if infoList?? && infoList?has_content>
        <div class="metadataQuality_info">
          <div class="qualityHeader d-flex justify-content-between">
            <div><i class="fa-solid fa-circle-info"> </i> OTHER ISSUES</div>
            <div><small>For absolute best practice, these issues should be addressed</small></div>
          </div>
          <ul class="list-group list-group-flush">
              <#list infoList as info>
                  <li class="list-group-item">${info.test}</li>
              </#list>
          </ul>
        </div>
      </#if>
  </#if>
</div>
