<#assign
    MD_checks = metadataQuality.check(id)
    problems = MD_checks.getProblems()
    errors = MD_checks.getErrors()
    cautions =  MD_checks.getCautions()
    warnings = MD_checks.getWarnings()
    alertclass="warning"
    alerticon="exclamation"
>

<#if errors gte 1 || warnings gte 1 || cautions gte 1>
    <#if errors gte 1 >
        <#assign alertclass="error" alerticon="times-circle">
    <#elseif cautions gte 1 >
        <#assign alertclass="caution" alerticon="exclamation-triangle">
    </#if>
    <div id="qualityAlert" class="hidden-print">
        <div class="alert alert-MDquality_${alertclass} alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close" title="dismiss"><span aria-hidden="true">
                <i class="fas fa-times"></i>
            </span></button>
            <i class="fas fa-${alerticon}"></i> <b>METADATA QUALITY: </b> 
            <#if errors gte 1>&nbsp;&nbsp;${errors} error<#if errors gt 1>s</#if>&nbsp;&nbsp;</#if>
            <#if cautions gte 1>&nbsp;&nbsp;${cautions} caution<#if cautions gt 1>s</#if>&nbsp;&nbsp;</#if>
            <#if warnings gte 1>&nbsp;&nbsp;${warnings} warning<#if warnings gt 1>s</#if>&nbsp;&nbsp;</#if>
            &nbsp;<a href="#section-metadataQuality">details</a>
        </div>
    </div>
</#if>
