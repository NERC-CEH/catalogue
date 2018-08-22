<div id="section-metadataQuality" class="clearfix hidden-print">
    <h3>Metadata quality</h3>
    <#if problems?? && problems?has_content>
        <#assign errors = func.filter(problems, "severity", "ERROR")>
        <#assign warnings = func.filter(problems, "severity", "WARNING")>
        <#assign infos = func.filter(problems, "severity", "INFO")>

        <div class="panel panel-info">
            <#if errors?? && errors?has_content>
                <div class="panel-heading MDquality_error">
                    <div class="row">
                        <div class="col-sm-3 col-xs-12"> <i class="fas fa-exclamation-triangle" > </i> ERRORS<br></div>
                        <div class="col-sm-9 hidden-xs"><small>These should always be fixed</small></div>
                    </div>
                </div>
                <ul class="list-group MDquality_error">
                    <#list errors as error>
                        <li class="list-group-item">${error.test}</li>
                    </#list>
                </ul>
            </#if>
            <#if warnings?? && warnings?has_content>
                <div class="panel-heading MDquality_warning">
                    <div class="row">
                        <div class="col-sm-3 col-xs-12"><i class="fas fa-exclamation" > </i> WARNINGS</div>
                        <div class="col-sm-9 hidden-xs"><small>These are problems but they <i>MAY</i> be acceptable in some circumstances - please check carefully</small></div>
                    </div>
                </div>
                <ul class="list-group MDquality_warning">
                    <#list warnings as warning>
                        <li class="list-group-item">${warning.test}</li>
                    </#list>
                </ul>
            </#if>
            <#if infos?? && infos?has_content>
                <div class="panel-heading MDquality_info">
                    <div class="row">
                        <div class="col-sm-3 col-xs-12"><i class="fas fa-info-circle" > </i> INFORMATION</div>
                        <div class="col-sm-9 hidden-xs"><small>For 100% metadata quality, these issues should be addressed</small></div>
                    </div>
                </div>
                <ul class="list-group MDquality_info">
                    <#list infos as info>
                        <li class="list-group-item">${info.test}</li>
                    </#list>
                </ul>
            </#if>
        </div>


    <#else>
        <p class="text-success"><i class="fas fa-check-circle"></i> No metadata issues found</p>
    </#if>
</div>