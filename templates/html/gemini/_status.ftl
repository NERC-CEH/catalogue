<#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn" || resourceStatus == "Embargoed">
    <div id="resourceStatus" class="alert alert-${resourceStatus}" role="alert">
        <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn">
            <p>
            <i class="fas fa-info-circle fa-lg"></i> <b>THIS ${recordType?upper_case} HAS BEEN ${resourceStatus?upper_case}</b>
                <#if revised??>
                by <a href="${revised.href}">${revised.title}</a>
                </#if>
            </p>
            <#if reasonChanged??>
                <p>${reasonChanged?html?replace("\n", "<br>")}</p>
            </#if>
            <p>If you need access to the archived version, please <a href="http://eidc.ceh.ac.uk/contact" target="_blank" rel="noopener noreferrer">contact the EIDC</a></p>
        <#elseif resourceStatus == "Embargoed">
        <p>
            <b>This ${recordType} is embargoed</b> 
            <#if datasetReferenceDate?? && datasetReferenceDate.releasedDate?has_content>
                and will be made available by ${datasetReferenceDate.releasedDate?date?string["d MMMM yyyy"]} at the latest
            </#if>
            &nbsp;<a href="http://eidc.ceh.ac.uk/help/faq/embargos" target="_blank" rel="noopener noreferrer" title="more information about embargos"><i class="fas fa-question-circle text-info"></i></a>
        </p>
        </#if>
    </div>
<#else>
    <div class="titleSpacer"></div>
</#if>