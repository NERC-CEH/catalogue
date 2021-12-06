<#assign
SA_checks = serviceAgreementQuality.check(id)
problems = SA_checks.getProblems()
errors = SA_checks.getErrors()
infos =  SA_checks.getInfo()
warnings = SA_checks.getWarnings()
alertclass="warning"
alerticon="exclamation"
>

<#if errors gte 1 || warnings gte 1 || infos gte 1>
    <#if errors gte 1 >
        <#assign alertclass="error" alerticon="exclamation-triangle">
    <#elseif warnings gte 1 >
        <#assign alertclass="warning" alerticon="exclamation">
    <#elseif infos gte 1 >
        <#assign alertclass="info" alerticon="info-circle">
    </#if>
    <div class="hidden-print">
        <div class="qualityalert qualityalert--${alertclass}" role="alert">
            <div class="qualityalert__header">
                <i class="fas fa-${alerticon}"></i> SERVICE AGREEMENT QUALITY
            </div>
            <div class="qualityalert__body">
                <#if errors gte 1>
                    <span>${errors} error<#if errors gt 1>s</#if></span>
                </#if>
                <#if warnings gte 1>
                    <span>${warnings} warning<#if warnings gt 1>s</#if></span>
                </#if>
                <#if infos gte 1>
                    <span>${infos} issue<#if infos gt 1>s</#if></span>
                </#if>
                &nbsp;<a href="#section-serviceAgreementQuality">details</a>
            </div>
        </div>
    </div>
</#if>
