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
    <div id="section-serviceAgreementQuality" class="clearfix hidden-print">
        <div class="panel">
            <div>
                <ul class="list-group">
                    <#if errors?? && errors?has_content>
                        <#list errors as error>
                            <li class="list-group-item alert alert--error">${error.test}</li>
                        </#list>       
                    </#if>
                    <#if warnings?? && warnings?has_content>
                        <#list warnings as warning>
                            <li class="list-group-item alert alert--warning">${warning.test}</li>
                        </#list>
                    </#if>
                    <#if infos?? && infos?has_content>
                        <#list infos as info>
                            <li class="list-group-item alert alert--info">${info.test}</li>
                        </#list>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</#if>