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

<#if problems?? && problems?has_content>
    <#assign errors = func.filter(problems, "severity", "ERROR")>
    <#assign warnings = func.filter(problems, "severity", "WARNING")>
    <#assign infos = func.filter(problems, "severity", "INFO")>
    <#if errors?? && errors?has_content>
        <div>This service agreement is incomplete and cannot be submitted until all errors are cleared</div>
    </#if>
    <div id="section-serviceAgreementQuality" class="clearfix d-print-none qualityReport">
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
