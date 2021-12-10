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

    <div id="section-serviceAgreementQuality" class="clearfix hidden-print">
        <div class="panel">
            <div class=" quality_error">
                <ul class="list-group">
                    <#if errors?? && errors?has_content>
                        <#list errors as error>
                            <li class="list-group-item error">${error.test}</li>
                        </#list>       
                    </#if>
                    <#if warnings?? && warnings?has_content>
                        <#list warnings as warning>
                            <li class="list-group-item warning">${warning.test}</li>
                        </#list>
                    </#if>
                    <#if infos?? && infos?has_content>
                        <#list infos as info>
                            <li class="list-group-item info">${info.test}</li>
                        </#list>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</#if>