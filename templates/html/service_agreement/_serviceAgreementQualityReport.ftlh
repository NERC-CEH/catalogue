<#import "../../functions.ftlh" as func>
<div id="section-serviceAgreementQuality" class="clearfix hidden-print">
    <#if problems?? && problems?has_content>
        <#assign errors = func.filter(problems, "severity", "ERROR")>
        <#assign warnings = func.filter(problems, "severity", "WARNING")>
        <#assign infos = func.filter(problems, "severity", "INFO")>

        <div class="panel">
            <#if errors?? && errors?has_content>
                <div class=" quality_error">
                    <div class="panel-heading ">
                        <div class="row">
                            <span class="error">ERRORS</span>
                        </div>
                    </div>
                    <ul class="list-group">
                        <#list errors as error>
                            <li class="list-group-item error">${error.test}</li>
                        </#list>
                    </ul>
                </div>
            </#if>
            <#if warnings?? && warnings?has_content>
                <div class=" quality_warning">
                    <div class="panel-heading">
                        <div class="row">
                            <i class="fas fa-exclamation" > </i> WARNINGS
                        </div>
                    </div>
                    <ul class="list-group">
                        <#list warnings as warning>
                            <li class="list-group-item">${warning.test}</li>
                        </#list>
                    </ul>
                </div>
            </#if>
            <#if infos?? && infos?has_content>
                <div class="quality_info">
                    <div class="panel-heading">
                        <i class="fas fa-info-circle" > </i> OTHER ISSUES
                    </div>
                    <ul class="list-group">
                        <#list infos as info>
                            <li class="list-group-item">${info.test}</li>
                        </#list>
                    </ul>
                </div>
            </#if>
        </div>
    </#if>
</div>