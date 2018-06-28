<#--  <#assign
    MD_checks = quality.check(id)
    problems = MD_checks.getProblems()
    errors = MD_checks.getErrors()
    warnings = MD_checks.getWarnings()
    alertclass="warning"
    alerticon="exclamation"
>

<#if errors gte 1 || warnings gte 1>
    <#if errors gte 1 >
        <#assign alertclass="danger" alerticon="exclamation-triangle">
    </#if>
    <div id="qualityAlert">
        <div class="alert alert-${alertclass} alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close" title="dismiss"><span aria-hidden="true">
                <i class="far fa-times-circle"></i>
            </span></button>
            <i class="fas fa-${alerticon}"></i> <b>METADATA QUALITY: </b> 
            <#if errors gte 1>${errors} error<#if errors gt 1>s</#if></#if>
            <#if errors gte 1 && warnings gte 1>and</#if>
            <#if warnings gte 1>${warnings} warning<#if warnings gt 1>s</#if></#if>
            &nbsp;<a href="#section-metadataQuality">&nbsp;details <i class="fas fa-angle-down"></i></a>
        </div>
    </div>
</#if>  -->
<#--  ${catalogues.retrieve('eidc')}  -->
metadata id ${quality.check('bb2d7874-7bf4-44de-aa43-348bd684a2fe')}