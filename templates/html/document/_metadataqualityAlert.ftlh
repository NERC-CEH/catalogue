<#assign
    MD_checks = metadataQuality.check(id)
    problems = MD_checks.getProblems()
    errors = MD_checks.getErrors()
    infos =  MD_checks.getInfo()
    warnings = MD_checks.getWarnings()
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
    <div class="qualityAlert d-print-none">
        <div class="alert alert-MDquality_${alertclass} alert-dismissible fade show" role="alert">
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" title="dismiss"><span aria-hidden="true">
            </span></button>
            <i class="fa-solid fa-${alerticon}"></i> <b>METADATA QUALITY</b>
            <#if errors gte 1>
                <span>${errors} error<#if errors gt 1>s</#if></span>
            </#if>
            <#if warnings gte 1>
                <span>${warnings} warning<#if warnings gt 1>s</#if></span>
            </#if>
            <#if infos gte 1>
                <span>${infos} issue<#if infos gt 1>s</#if></span>
            </#if>
            &nbsp;<a href="#section-metadataQuality">details</a>
        </div>
    </div>
</#if>
