<#if problems?? && problems?has_content>
  <div id="segment-metadataQuality" class="clearfix d-print-none metadataQuality metadataQuality-report my-3">
    <h2>Metadata quality report</h2>
      <#if errorList??>
        <@qualityCard errorList "error" "fa-circle-info" "ERRORS" "These should always be fixed"/>
      </#if>

      <#if warningList??>
        <@qualityCard warningList "warning" "fa-circle-info" "WARNINGS" "These are usually problems but in some circumstances they MAY be acceptable - please check carefully"/>
      </#if>

      <#if infoList??>
        <@qualityCard infoList "info" "fa-circle-info" "OTHER ISSUES" "For absolute best practice, these issues should be addressed"/>
      </#if>
  </div>
</#if>

<#macro qualityCard data qualityClass qualityIcon title subtitle="">
  <#if data?has_content>
    <div class="card card-metadataQuality card-metadataQuality-${qualityClass} mb-3">
      <div class="card-header">
        <i class="fa-solid ${qualityIcon}"> </i> ${title}<br>
        <small>${subtitle}</small>
      </div>
      <ul class="list-group list-group-flush">
          <#list data as item>
              <li class="list-group-item">${item.test}</li>
          </#list>
      </ul>
    </div>
  </#if>
</#macro>

