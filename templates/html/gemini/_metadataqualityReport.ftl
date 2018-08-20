<div id="section-metadataQuality" class="clearfix hidden-print">
    <h3>Metadata quality</h3>
    <#if problems?? && problems?has_content>
        <#assign errors = func.filter(problems, "severity", "ERROR")>
        <#assign cautions = func.filter(problems, "severity", "CAUTION")>
        <#assign warnings = func.filter(problems, "severity", "WARNING")>

        <div class="panel panel-info">
            <#if errors?? && errors?has_content>
                <div class="panel-heading MDquality_error">
                    <i class="fas fa-times-circle" > </i> ERRORS<br><small>These should be corrected</small>
                </div>
                <ul class="list-group MDquality_error">
                    <#list errors as error>
                        <li class="list-group-item">${error.test}</li>
                    </#list>
                </ul>
            </#if>
            <#if cautions?? && cautions?has_content>
                <div class="panel-heading MDquality_caution">
                    <i class="fas fa-exclamation-triangle" > </i> CAUTIONS <small><br>These are errors but they <i>MAY</i> be acceptable in some circumstances - please check carefully</small>
                </div>
                <ul class="list-group MDquality_caution">
                    <#list cautions as caution>
                        <li class="list-group-item">${caution.test}</li>
                    </#list>
                </ul>
            </#if>
            <#if warnings?? && warnings?has_content>
                <div class="panel-heading MDquality_warning">
                    <i class="fas fa-exclamation" > </i> WARNINGS
                </div>
                <ul class="list-group MDquality_warning">
                    <#list warnings as warning>
                        <li class="list-group-item">${warning.test}</li>
                    </#list>
                </ul>
            </#if>
        </div>


    <#else>
        <p class="text-success"><i class="fas fa-check-circle"></i> No metadata issues found</p>
    </#if>
</div>