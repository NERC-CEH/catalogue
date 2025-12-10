<#import "../../functions.ftlh" as func>
<#assign
    SA_checks = serviceAgreementQuality.check(id)
    problems = SA_checks.getProblems()
    errors = SA_checks.getErrors()
    infos =  SA_checks.getInfo()
    warnings = SA_checks.getWarnings()
    alertclass="warning"
    alerticon="exclamation"
>

<div class="d-print-none">
  <#if problems?? && problems?has_content>
      <#assign errors = SA_checks.getErrorList()>
      <#assign warnings = SA_checks.getWarningList()>
      <#assign infos = SA_checks.getInfoList()>
      <#if errors?? && errors?has_content>
          <div class="alert alert-dark border-4 border-primary mt-2">
            <i class="fa-solid fa-info-circle text-primary"></i> <b>This service agreement is incomplete and cannot be submitted until all errors are cleared</b>
          </div>
      </#if>
      <div id="segment-serviceAgreementQuality" class="clearfix qualityReport">
          <#if errors?? && errors?has_content>
              <#list errors as error>
                  <div class="qualityItem qualityItem--error">
                      <div class="fa-solid fa-triangle-exclamation"> </div>
                      <div>${error.test}</div>
                  </div>
              </#list>
          </#if>
          <#if warnings?? && warnings?has_content>
              <#list warnings as warning>
                  <div class="qualityItem qualityItem--warning">
                      <div class="fa-solid fa-circle-exclamation"> </div>
                      <div>${warning.test}</div>
                  </div>
              </#list>
          </#if>
          <#if infos?? && infos?has_content>
              <#list infos as info>
                  <div class="qualityItem qualityItem--info">
                      <div class="fa-solid fa-info"> </div>
                      <div>${info.test}</div>
                  </div>
              </#list>
          </#if>
      </div>
  </#if>
</div>
